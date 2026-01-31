package com.jongsoft.finance.classification.domain.model;

import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.classification.domain.commands.CreateCategoryCommand;
import com.jongsoft.finance.classification.domain.commands.DeleteCategoryCommand;
import com.jongsoft.finance.classification.domain.commands.RenameCategoryCommand;
import com.jongsoft.lang.Control;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class Category implements Classifier {

    private Long id;
    private String label;
    private String description;
    private boolean delete;

    private Category(String label, String description) {
        this.label = label;
        CreateCategoryCommand.categoryCreated(label, description);
    }

    Category(Long id, String label, String description, boolean delete) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.delete = delete;
    }

    public void rename(String label, String description) {
        var hasChanged = Control.Equal(this.label, label)
                .append(this.description, description)
                .isNotEqual();

        if (hasChanged) {
            this.label = label;
            this.description = description;
            RenameCategoryCommand.categoryRenamed(id, label, description);
        }
    }

    public void remove() {
        this.delete = true;
        DeleteCategoryCommand.categoryDeleted(id);
    }

    @Override
    public String toString() {
        return getLabel();
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDelete() {
        return delete;
    }

    public static Category create(String label, String description) {
        return new Category(label, description);
    }
}
