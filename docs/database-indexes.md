# Database Indexes

## Query Analysis

The main Observation search is implemented by `ReportSpecifications` and
`ObservationService`. It supports:

- pagination ordered by `date_time DESC, report_id DESC`;
- an inclusive `date_time` range;
- species scientific/common-name filtering;
- location city/state/biome filtering;
- taxonomy filtering through Species -> Genus -> Family -> Order -> Class ->
  Phylum;
- exact legacy queries by report species, location, user, and date range.

The schema is currently managed by Hibernate with
`spring.jpa.hibernate.ddl-auto=update`. Flyway dependencies are present, but the
project does not have an existing migration history, so indexes are declared
with JPA `@Table(indexes = ...)` annotations.

## Added Indexes

| Index | Columns | Query supported |
| --- | --- | --- |
| `idx_report_date_id` | `report(date_time, report_id)` | Unfiltered pagination, date ranges, and the default newest-first ordering |
| `idx_report_species_date_id` | `report(species, date_time, report_id)` | Exact species report lookup followed by newest-first ordering |
| `idx_report_location_date_id` | `report(location_id, date_time, report_id)` | Exact location report lookup followed by newest-first ordering |
| `idx_report_user_date_id` | `report(user_id, date_time, report_id)` | User-owned report lookup and newest-first ordering |
| `idx_species_common_name` | `species(common_name)` | Existing exact common-name repository query |
| `idx_species_genus` | `species(genus_id)` | Reverse traversal from a matching genus to its species |
| `idx_location_city` | `location(city)` | Existing exact city repository query |
| `idx_genus_family` | `genus(family_id)` | Reverse taxonomy traversal from family to genus |
| `idx_family_order` | `family(order_id)` | Reverse taxonomy traversal from order to family |
| `idx_taxonomy_order_class` | `taxonomy_order(class_id)` | Reverse taxonomy traversal from class to order |
| `idx_taxonomy_class_phylum` | `taxonomy_class(phylum_id)` | Reverse taxonomy traversal from phylum to class |

Primary keys such as `report_id`, `species_id`, `genus_id`, and the other
taxonomy IDs are already indexed, so no duplicate indexes were added for them.
Low-selectivity fields such as `verified`, `extinct_status`, and
`age_approximation` were deliberately left unindexed.

## Manual MySQL SQL

With the current `ddl-auto=update` configuration, Hibernate creates missing
indexes during application startup. If a deployment changes
`JPA_DDL_AUTO` to `validate` or `none`, apply the equivalent SQL manually after
checking existing index names with `SHOW INDEX`:

```sql
CREATE INDEX idx_report_date_id
    ON report (date_time, report_id);
CREATE INDEX idx_report_species_date_id
    ON report (species, date_time, report_id);
CREATE INDEX idx_report_location_date_id
    ON report (location_id, date_time, report_id);
CREATE INDEX idx_report_user_date_id
    ON report (user_id, date_time, report_id);

CREATE INDEX idx_species_common_name
    ON species (common_name);
CREATE INDEX idx_species_genus
    ON species (genus_id);
CREATE INDEX idx_location_city
    ON location (city);

CREATE INDEX idx_genus_family
    ON genus (family_id);
CREATE INDEX idx_family_order
    ON family (order_id);
CREATE INDEX idx_taxonomy_order_class
    ON taxonomy_order (class_id);
CREATE INDEX idx_taxonomy_class_phylum
    ON taxonomy_class (phylum_id);
```

Verify the installed indexes:

```sql
SHOW INDEX FROM report;
SHOW INDEX FROM species;
SHOW INDEX FROM location;
SHOW INDEX FROM genus;
SHOW INDEX FROM family;
SHOW INDEX FROM taxonomy_order;
SHOW INDEX FROM taxonomy_class;
```

## Limitation Of Contains Filters

The REST filters currently use expressions such as:

```sql
LOWER(common_name) LIKE '%lion%'
```

A normal B-tree index cannot efficiently serve a leading-wildcard search.
Therefore, the indexes above improve joins, exact lookups, date ranges, and
sorting, but they do not eliminate scans for the current contains-style species,
location, or taxonomy filters. If contains search becomes a bottleneck, evaluate
MySQL `FULLTEXT` indexes or a dedicated normalized search column as a separate
change.

## DataFaker Query Timing Test

The project already includes `net.datafaker:datafaker`. A simple repeatable
comparison can be performed against local MySQL:

1. Start MySQL and the application with `docker compose up --build -d`.
2. Create enough prerequisite users, species, and locations for report
   generation.
3. Use `net.datafaker.Faker` in a temporary integration test or development
   runner to create batches of 10,000 to 100,000 `Report` rows. Reuse existing
   users/species/locations and generate the comment and timestamp, for example:

```java
Faker faker = new Faker();
Report report = new Report();
report.setUser(users.get(random.nextInt(users.size())));
report.setSpecies(species.get(random.nextInt(species.size())));
report.setLocation(locations.get(random.nextInt(locations.size())));
report.setComment(faker.animal().name());
report.setDateTime(
        OffsetDateTime.now(ZoneOffset.UTC)
                .minusDays(random.nextInt(3650)));
reportRepository.save(report);
```

4. Insert in batches and call `flush()`/`clear()` every 1,000 records so the JPA
   persistence context does not dominate memory usage.
5. Warm each query three times, then measure at least ten runs with
   `System.nanoTime()`. Compare median rather than a single run:

```java
long started = System.nanoTime();
observationService.findObservations(
        0, 20, null, null, startDate, endDate, null);
long elapsedMs = (System.nanoTime() - started) / 1_000_000;
```

6. Test at least these scenarios:
   - newest 20 observations without filters;
   - a one-month date range;
   - exact species/location repository queries;
   - taxonomy contains search, which is expected to remain scan-heavy.
7. Use MySQL `EXPLAIN ANALYZE` to confirm index selection:

```sql
EXPLAIN ANALYZE
SELECT *
FROM report
WHERE date_time BETWEEN '2025-01-01' AND '2025-02-01'
ORDER BY date_time DESC, report_id DESC
LIMIT 20;
```

For a before/after comparison, run the same dataset and query list on a commit
before the index annotations and on the indexed commit. Keep the Docker volume,
MySQL version, JVM options, page size, and warm-up count identical.
