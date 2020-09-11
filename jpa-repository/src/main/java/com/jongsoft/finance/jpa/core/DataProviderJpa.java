package com.jongsoft.finance.jpa.core;

import javax.persistence.EntityManager;

import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.jpa.FilterDelegate;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public abstract class DataProviderJpa<T, Y extends EntityJpa>
        extends RepositoryJpa {

    private final EntityManager entityManager;
    private final Class<Y> forClass;

    public DataProviderJpa(EntityManager entityManager, Class<Y> forClass) {
        this.entityManager = entityManager;
        this.forClass = forClass;
    }

    public Optional<T> lookup(long id) {
        var query = entityManager.createQuery("from " + forClass.getSimpleName() + " where id = :id");

        query.setParameter("id", id);

        return API.Option(convert(singleValue(query)));
    }

    protected ResultPage<T> queryPage(
            FilterDelegate<?> filterDelegate,
            Optional<Integer> offset,
            Optional<Integer> limit) {
        var countHql = "select count(distinct a.id) " + filterDelegate.generateHql();
        var hql = new StringBuilder("select distinct a ")
                .append(filterDelegate.generateHql());

        if (filterDelegate.sort() != null) {
            hql.append(" order by ");
            for (var iterator = filterDelegate.sort().getOrderBy().iterator(); iterator.hasNext();) {
                var order = iterator.next();
                hql.append(order.getProperty()).append(" ").append(order.isAscending() ? "asc" : "desc");
                if (iterator.hasNext()) {
                    hql.append(", ");
                }
            }
        }

        var query = entityManager.createQuery(hql.toString());
        var countquery = entityManager.createQuery(countHql);

        filterDelegate.prepareQuery(query);
        filterDelegate.prepareQuery(countquery);

        if (offset.isPresent() && limit.isPresent()) {
            query.setFirstResult(offset.get());
            query.setMaxResults(limit.get());
        }

        long hits = singleValue(countquery);
        Sequence<T> page = this.<Y>multiValue(query).map(this::convert);

        return new ResultPage<>() {
            @Override
            public int pages() {
                return limit.isPresent() ? (int) hits / limit.get() : 1;
            }

            @Override
            public long total() {
                return hits;
            }

            @Override
            public boolean hasNext() {
                return hits > (offset.getOrSupply(() -> 0) + page.size());
            }

            @Override
            public Sequence<T> content() {
                return page;
            }
        };
    }


    protected abstract T convert(Y source);
}
