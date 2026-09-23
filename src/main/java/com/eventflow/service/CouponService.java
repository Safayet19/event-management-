package com.eventflow.service;

import com.eventflow.dto.CouponForm;
import com.eventflow.model.Coupon;
import com.eventflow.model.Event;
import com.eventflow.repository.CouponRepository;
import com.eventflow.repository.RegistrationRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CouponService {

    public record AppliedCoupon(String couponId, String couponCode, BigDecimal discountAmount, BigDecimal finalPrice) { }

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9_-]{3,20}$");
    private static final Set<String> DISCOUNT_TYPES = Set.of("PERCENT", "FIXED");

    private final CouponRepository couponRepository;
    private final RegistrationRepository registrationRepository;

    public CouponService(CouponRepository couponRepository,
                         RegistrationRepository registrationRepository) {
        this.couponRepository = couponRepository;
        this.registrationRepository = registrationRepository;
    }

    public List<Coupon> findAll() {
        return couponRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Coupon> findByOrganizer(String organizerId) {
        return couponRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId);
    }

    public List<Coupon> findByEvent(String eventId) {
        return couponRepository.findByEventIdOrderByCreatedAtDesc(eventId);
    }

    public Coupon create(Event event, CouponForm form) {
        if (!event.isPaid()) {
            throw new IllegalArgumentException("Coupons can only be created for paid events.");
        }
        if ("COMPLETED".equals(event.getStatus())) {
            throw new IllegalArgumentException("Coupons cannot be created for completed events.");
        }
        Coupon coupon = new Coupon();
        coupon.setEventId(event.getId());
        coupon.setOrganizerId(event.getOrganizerId());
        applyFields(coupon, form, true);
        try {
            return couponRepository.save(coupon);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("This coupon code already exists for the selected event.");
        }
    }

    public Coupon update(Event event, String couponId, CouponForm form) {
        if (!event.isPaid()) {
            throw new IllegalArgumentException("Coupons can only be used with paid events.");
        }
        if ("COMPLETED".equals(event.getStatus())) {
            throw new IllegalArgumentException("Coupons cannot be edited for completed events.");
        }
        Coupon coupon = requireForEvent(event.getId(), couponId);
        applyFields(coupon, form, false);
        try {
            return couponRepository.save(coupon);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("This coupon code already exists for the selected event.");
        }
    }

    public void delete(Event event, String couponId) {
        if ("COMPLETED".equals(event.getStatus())) {
            throw new IllegalArgumentException("Coupons for completed events are read-only.");
        }
        Coupon coupon = requireForEvent(event.getId(), couponId);
        long uses = registrationRepository.countByCouponId(couponId);
        if (uses > 0) {
            throw new IllegalArgumentException("This coupon has already been used. Pause it instead of deleting it.");
        }
        couponRepository.delete(coupon);
    }

    public AppliedCoupon apply(Event event, BigDecimal ticketPrice, String couponCode) {
        if ("COMPLETED".equals(event.getStatus())) {
            throw new IllegalArgumentException("Coupons cannot be applied to completed events.");
        }
        BigDecimal original = ticketPrice == null ? BigDecimal.ZERO : ticketPrice.setScale(2, RoundingMode.HALF_UP);
        String cleanCode = normalizeCode(couponCode);

        if (cleanCode.isBlank()) {
            return new AppliedCoupon(null, null, BigDecimal.ZERO.setScale(2), original.max(BigDecimal.ZERO));
        }
        if (!event.isPaid() || original.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Coupon codes apply to paid tickets only.");
        }

        Coupon coupon = couponRepository.findByEventIdAndCodeIgnoreCase(event.getId(), cleanCode)
                .orElseThrow(() -> new IllegalArgumentException("Coupon code is invalid for this event."));

        validateUsable(coupon);

        BigDecimal discount;
        if ("PERCENT".equals(coupon.getDiscountType())) {
            discount = original.multiply(coupon.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue().setScale(2, RoundingMode.HALF_UP);
        }
        if (discount.compareTo(original) > 0) discount = original;
        if (discount.compareTo(BigDecimal.ZERO) < 0) discount = BigDecimal.ZERO;

        BigDecimal finalPrice = original.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return new AppliedCoupon(coupon.getId(), coupon.getCode(), discount, finalPrice);
    }

    public long usageCount(String couponId) {
        return registrationRepository.countByCouponId(couponId);
    }

    public Map<String, Long> usageCounts(List<Coupon> coupons) {
        if (coupons == null || coupons.isEmpty()) return Map.of();
        return coupons.stream().collect(Collectors.toMap(Coupon::getId, coupon -> usageCount(coupon.getId())));
    }

    public long activeCount(List<Coupon> coupons) {
        if (coupons == null) return 0;
        return coupons.stream().filter(coupon -> coupon.isActive() && !coupon.isExpired()).count();
    }

    public void pauseAllByEvent(String eventId) {
        List<Coupon> coupons = findByEvent(eventId);
        boolean changed = false;
        for (Coupon coupon : coupons) {
            if (coupon.isActive()) {
                coupon.setActive(false);
                coupon.setUpdatedAt(LocalDateTime.now());
                changed = true;
            }
        }
        if (changed) couponRepository.saveAll(coupons);
    }

    public void deleteAllByEvent(String eventId) {
        couponRepository.deleteAllByEventId(eventId);
    }

    private Coupon requireForEvent(String eventId, String couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found."));
        if (!eventId.equals(coupon.getEventId())) {
            throw new IllegalArgumentException("This coupon does not belong to the event.");
        }
        return coupon;
    }

    private void validateUsable(Coupon coupon) {
        if (!coupon.isActive()) {
            throw new IllegalArgumentException("This coupon is currently paused.");
        }
        if (coupon.isExpired()) {
            throw new IllegalArgumentException("This coupon has expired.");
        }
        if (coupon.getUsageLimit() > 0 && registrationRepository.countByCouponId(coupon.getId()) >= coupon.getUsageLimit()) {
            throw new IllegalArgumentException("This coupon has reached its usage limit.");
        }
    }

    private void applyFields(Coupon coupon, CouponForm form, boolean creating) {
        if (form == null) throw new IllegalArgumentException("Enter coupon information.");

        String code = normalizeCode(form.getCode());
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException("Coupon code must be 3-20 characters using letters, numbers, - or _.");
        }

        String discountType = form.getDiscountType() == null ? "" : form.getDiscountType().trim().toUpperCase(Locale.ROOT);
        if (!DISCOUNT_TYPES.contains(discountType)) {
            throw new IllegalArgumentException("Choose a valid discount type.");
        }

        BigDecimal discountValue;
        try {
            discountValue = new BigDecimal(form.getDiscountValue() == null ? "" : form.getDiscountValue().trim())
                    .setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Enter a valid discount value.");
        }

        if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount value must be greater than 0.");
        }
        if ("PERCENT".equals(discountType) && discountValue.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100%.");
        }
        if ("FIXED".equals(discountType) && discountValue.compareTo(new BigDecimal("10000000")) > 0) {
            throw new IllegalArgumentException("Fixed discount is too large.");
        }

        LocalDate expiryDate;
        try {
            expiryDate = LocalDate.parse(form.getExpiryDate());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Choose a valid coupon expiry date.");
        }
        if (expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Coupon expiry date cannot be in the past.");
        }

        int usageLimit = form.getUsageLimit();
        if (usageLimit < 1 || usageLimit > 100000) {
            throw new IllegalArgumentException("Usage limit must be between 1 and 100000.");
        }
        if (!creating) {
            long used = registrationRepository.countByCouponId(coupon.getId());
            if (usageLimit < used) {
                throw new IllegalArgumentException("Usage limit cannot be lower than the number of existing redemptions.");
            }
        }

        coupon.setCode(code);
        coupon.setDiscountType(discountType);
        coupon.setDiscountValue(discountValue);
        coupon.setExpiryDate(expiryDate);
        coupon.setUsageLimit(usageLimit);
        coupon.setActive(form.isActive());
        coupon.setUpdatedAt(LocalDateTime.now());
        if (coupon.getCreatedAt() == null) coupon.setCreatedAt(LocalDateTime.now());
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }
}
