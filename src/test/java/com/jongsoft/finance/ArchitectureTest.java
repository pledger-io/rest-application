package com.jongsoft.finance;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.Configuration.consideringOnlyDependenciesInDiagram;
import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.adhereToPlantUmlDiagram;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import com.tngtech.archunit.core.importer.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.regex.Pattern;

@DisplayName("Architecture rules")
class ArchitectureTest {

    private static final JavaClasses classes = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .withImportOption(new ImportOption() {
            static final Pattern NO_GENERATED_CLASSES = Pattern.compile(".*\\$.*\\$Definition.*");
            @Override
            public boolean includes(Location location) {
                // exclude generated classes by Micronaut
                return !location.matches(NO_GENERATED_CLASSES);
            }
        })
        .importPackages("com.jongsoft.finance");


    @Test
    @DisplayName("Validate no cycles")
    void validateNoCycles() {
        slices()
            .matching("com.jongsoft.finance.(*)..")
            .should()
            .beFreeOfCycles();
    }

    @Test
    @DisplayName("Validate layer constraints using architecture diagram")
    void validateLayerConstraints() {
        URL umlDiagramLocation = getClass().getResource("/architecture/layer-design.puml");
        classes()
            .should(
                adhereToPlantUmlDiagram(
                    umlDiagramLocation,
                    consideringOnlyDependenciesInDiagram()))
            .check(classes);
    }

    @Test
    @DisplayName("Validate module layering")
    void validateModuleLayering() {
        layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("SuggestionModule").definedBy("..finance.suggestion..")
            .layer("ExportModule").definedBy("..finance.exporter..")
            .layer("BudgetModule").definedBy("..finance.budget..")
            .layer("ClassificationModule").definedBy("..finance.classification..")
            .layer("ContractModule").definedBy("..finance.contract..")
            .layer("BankingModule").definedBy("..finance.banking..")
            .layer("ProjectModule").definedBy("..finance.project..")
            .layer("InvoiceModule").definedBy("..finance.invoice..")
            .layer("CoreModule").definedBy("..finance.core..")
            .whereLayer("BankingModule").mayOnlyAccessLayers("CoreModule", "BankingModule")
            .whereLayer("ContractModule").mayOnlyAccessLayers("CoreModule", "BankingModule", "ContractModule")
            .whereLayer("BudgetModule").mayOnlyAccessLayers("CoreModule", "BankingModule", "BudgetModule")
            .whereLayer("ClassificationModule").mayOnlyAccessLayers("CoreModule", "BankingModule", "ClassificationModule")
            .whereLayer("ProjectModule").mayOnlyAccessLayers("CoreModule", "BankingModule", "ProjectModule")
            .whereLayer("InvoiceModule").mayOnlyAccessLayers("CoreModule", "BankingModule", "ProjectModule", "InvoiceModule")
            .whereLayer("ExportModule").mayOnlyAccessLayers("CoreModule", "BankingModule", "ClassificationModule", "ContractModule", "BudgetModule", "SuggestionModule", "ProjectModule", "InvoiceModule")
            .whereLayer("SuggestionModule").mayOnlyAccessLayers("CoreModule", "BankingModule", "ClassificationModule", "ContractModule", "BudgetModule", "SuggestionModule")
            .whereLayer("CoreModule").mayNotAccessAnyLayer()
            .check(classes);
    }
}
