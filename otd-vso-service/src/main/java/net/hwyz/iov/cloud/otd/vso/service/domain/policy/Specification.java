package net.hwyz.iov.cloud.otd.vso.service.domain.policy;

/**
 * 规约接口（规格模式）
 *
 * @author VSO Team
 */
public interface Specification<T> {

    /**
     * 判断是否满足规约
     *
     * @param candidate 候选对象
     * @return 是否满足
     */
    boolean isSatisfiedBy(T candidate);

    /**
     * 与规约
     */
    default Specification<T> and(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }

    /**
     * 或规约
     */
    default Specification<T> or(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

    /**
     * 非规约
     */
    default Specification<T> not() {
        return candidate -> !this.isSatisfiedBy(candidate);
    }

}
