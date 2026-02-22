package com.jongsoft.finance;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.Configuration.consideringOnlyDependenciesInDiagram;
import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.adhereToPlantUmlDiagram;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;

@DisplayName("Architecture rules")
class ArchitectureTest {

    private static final JavaClasses classes = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
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
            .layer("CoreModule").definedBy("..finance.core..")
            .whereLayer("BankingModule").mayOnlyAccessLayers("CoreModule", "BankingModule")
            .whereLayer("ContractModule").mayOnlyAccessLayers("CoreModule", "BankingModule", "ContractModule")
            .whereLayer("BudgetModule").mayOnlyAccessLayers("CoreModule", "BankingModule", "BudgetModule")
            .whereLayer("ClassificationModule").mayOnlyAccessLayers("CoreModule", "BankingModule", "ClassificationModule")
            .whereLayer("ExportModule").mayOnlyAccessLayers("CoreModule", "BankingModule", "ClassificationModule", "ContractModule", "BudgetModule", "SuggestionModule")
            .whereLayer("SuggestionModule").mayOnlyAccessLayers("CoreModule", "BankingModule", "ClassificationModule", "ContractModule", "BudgetModule", "SuggestionModule")
            .whereLayer("CoreModule").mayNotAccessAnyLayer()
            .check(classes);
    }
}
