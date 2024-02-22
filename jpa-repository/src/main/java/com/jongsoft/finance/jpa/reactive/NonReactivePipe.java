package com.jongsoft.finance.jpa.reactive;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.jpa.ResultPageImpl;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

/**
 * The non reactive JPA pipeline can be used to obtain entities from the database in a blocking manner. This means that
 * once a query gets executed the caller will wait until the query completes. The result will be returned instantly and can
 * be used directly.
 *
 * @param <T> the type of JPA entity this pipeline supports
 */
@RequiredArgsConstructor
public class NonReactivePipe<T> extends JpaPipe<T, NonReactivePipe<T>> {

    private final EntityManager entityManager;

    /**
     * Run the query in the pipeline and convert the result into an optional.
     *
     * @return either an empty optional or an optional containing the resolved entity.
     */
    public Optional<T> maybe() {
        return maybe(Function.identity());
    }

    /**
     * Run the query in the pipeline and convert the result into an optional. Additionally apply the mapping logic
     * to the entity as provided with the {@code converter}. The conversion will occur from the JPA entity {@code T}
     * to the provided type of {@code R}.
     *
     * @param <R> the type to be returned
     * @param converter the conversion logic
     * @return either an empty optional or an optional containing the resolved entity.
     */
    @SuppressWarnings("unchecked")
    public <R> Optional<R> maybe(Function<T, R> converter) {
        var query = entityManager.createQuery(hql());

        applyParameters(query);
        applyPaging(query);

        return Control.Try(() -> (T) query.getSingleResult())
                .map(converter)
                .map(Control::Option)
                .recover(e -> {
                    LoggerFactory.getLogger(NonReactivePipe.class)
                            .trace("Unable to find entity, cause: {}", e.getLocalizedMessage());
                    return Control.Option();
                })
                .get();
    }

    /**
     * Run the provided query in the pipeline, returning a sequence of entities that are found in the
     * database.
     *
     * @return a sequence with all found entities, or empty in case none are found
     */
    @SuppressWarnings("unchecked")
    public Sequence<T> sequence() {
        var query = entityManager.createQuery(hql() + sort());

        applyParameters(query);
        applyPaging(query);

        return Collections.List(query.getResultList());
    }

    /**
     * Run a query on the database, supporting paging. Here the pagination is provided by the pipeline using the
     * {@link #offset(int)} and the {@link #limit()}.
     *
     * @return a paged result with entities, or empty page when no entities match the query
     */
    @SuppressWarnings("unchecked")
    public ResultPage<T> page() {
        var countHql = "select count(distinct a.id) " + hql();
        var selectHql = "select distinct a " + hql() + sort();

        var countQuery = entityManager.createQuery(countHql, Long.class);
        var selectQuery = entityManager.createQuery(selectHql);

        applyParameters(countQuery);
        applyParameters(selectQuery);
        applyPaging(selectQuery);

        long hits = countQuery.getSingleResult();

        return new ResultPageImpl<>(
                Collections.List((List<T>) selectQuery.getResultList()),
                limit(),
                hits);
    }

    @Override
    protected NonReactivePipe<T> self() {
        return this;
    }
}
