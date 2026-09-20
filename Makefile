.PHONY: help build test clean lint check coverage jacoco ci format install-hooks verify

GRADLE := ./gradlew --console=plain
PRE_COMMIT := pre-commit

help:
	@echo "Available targets:"
	@echo "  build          - Compile the project"
	@echo "  test           - Run Spock/Spock tests"
	@echo "  check          - Run lint + test"
	@echo "  lint           - Run pre-commit hooks"
	@echo "  format         - Auto-fix lint issues"
	@echo "  coverage       - Run tests with JaCoCo coverage report"
	@echo "  jacoco         - Alias for coverage"
	@echo "  clean          - Remove build artifacts"
	@echo "  install-hooks  - Install pre-commit hooks"
	@echo "  verify         - Run tests with Gradle verification"
	@echo "  ci             - Run full CI pipeline (lint + build + test + coverage)"
	@echo "  all            - Alias for ci"

build:
	$(GRADLE) build -x test

test:
	$(GRADLE) test

lint:
	$(PRE_COMMIT) run --all-files

format:
	$(PRE_COMMIT) run --all-files

check: lint test

coverage:
	$(GRADLE) test jacocoTestReport

jacoco: coverage

clean:
	$(GRADLE) clean
	rm -rf build/ .gradle/

install-hooks:
	$(PRE_COMMIT) install

verify:
	$(GRADLE) check

ci: lint build test coverage

all: ci
