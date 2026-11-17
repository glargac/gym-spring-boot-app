package com.wellness.admin;

import com.wellness.web.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * TEMPLATE METHOD pattern. This class owns the SKELETON of every admin CRUD operation (validate, then build or load,
 * then save, then map to a view, all inside one transaction). The steps that differ between a workout template and a
 * meal plan are abstract methods that each subclass fills in. Control flows from the base class DOWN to the subclass
 * ("Hollywood principle": don't call us, we'll call you).
 *
 * Template Method vs. Strategy: Template Method varies steps through INHERITANCE (fixed at compile time, one
 * hierarchy); Strategy varies them through COMPOSITION (swappable at runtime, any number of strategies).
 * The recommendation engine in com.wellness.plan uses Strategy, this class uses Template Method.
 *
 * Spring detail: the skeleton methods are deliberately NOT final. @Transactional works through a CGLIB subclass
 * proxy, and a final method cannot be overridden by that proxy, so the transaction would be silently skipped.
 *
 * @param <E>    the JPA aggregate root
 * @param <REQ>  the request record the admin sends
 * @param <VIEW> the read model returned to the admin
 */
public abstract class AdminPlanService<E, REQ, VIEW> {

    private final JpaRepository<E, Long> repository;
    private final String label;

    protected AdminPlanService(JpaRepository<E, Long> repository, String label) {
        this.repository = repository;
        this.label = label;
    }

    // ---------------------------------------- the skeleton (same for every plan type) ----------------------------------------

    @Transactional(readOnly = true)
    public List<VIEW> list() {
        return repository.findAll(Sort.by("id")).stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public VIEW get(Long id) {
        return toView(find(id));
    }

    @Transactional
    public VIEW create(REQ request) {
        validate(request);
        E entity = newEntity(request);
        return toView(repository.saveAndFlush(entity)); // flush so generated ids exist for the response
    }

    /** Replace semantics (PUT): the old state is overwritten with the request, in one transaction. */
    @Transactional
    public VIEW update(Long id, REQ request) {
        validate(request);
        E entity = find(id);
        replaceState(entity, request);
        return toView(repository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id)); // cascade REMOVE deletes the children
    }

    // ---------------------------------------- the steps subclasses provide ----------------------------------------

    /** A HOOK: has a default (do nothing), a subclass overrides it only if it has extra rules. */
    protected void validate(REQ request) {
    }

    /** Builds a brand-new aggregate from the request (resolving catalog codes, creating the children). */
    protected abstract E newEntity(REQ request);

    /** Overwrites an existing aggregate with the request (changes the fields, replaces the children). */
    protected abstract void replaceState(E entity, REQ request);

    /** Maps the aggregate to its read model. Called inside the transaction, so lazy children can still be read. */
    protected abstract VIEW toView(E entity);

    private E find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(label + " " + id + " not found"));
    }
}
