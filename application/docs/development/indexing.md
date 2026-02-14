# B-Fabric Documentation — Indexing

This document explains how to add a new document type to the B-Fabric search index. To make a class searchable, implement the indexer contracts and provide the mapping from your entity to index fields. The steps below describe required methods and where to register the mapping.

## Steps

1. Preconditions
    - Implement the `org.bfabric.indexer.api.Indexable` interface on the class to be indexed.

2. Define searchable fields
    - Implement `getIndexFields()` to return the field names that should appear in the advanced search UI.
    - Implement `getIndexListingFields()` to return the field names (in the desired order) used for condensed listing results when filtering by this document type.

3. Provide the index mapping
    - In `org.bfabric.indexer.IndexDocumentHelper` add a `getIndex` method for your new document type.
    - The `getIndex` method should build and return an `IndexMap` that maps each index field name to the value to be stored in the index.
    - Include all fields that must be searchable, sortable, or displayed in listings.

4. Register type metadata
    - Implement `getIndexMapEnum()` to return an Enum entry describing the indexed type. This Enum should include metadata such as the Java class and the logical entity type used by the indexer.

5. Triggering index updates
    - Use the static helper methods in `IndexHelper` to trigger index updates, reindexing, or removals when entities change.

## Testing

- After implementing the mapping, perform an index update and verify:
    - Fields appear in advanced search and listing views.
    - Search results include expected entries and sorting works as intended.

## Notes

- Keep field names stable: changing indexed field names requires reindexing existing data.
- Only include the data necessary for search and display to keep the index compact.