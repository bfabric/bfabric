/*
    MIT License

    Copyright (c) 2005-2026 Functional Genomics Center Zurich, UZH/ETH Zurich

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package org.bfabric.indexer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.BytesRef;
import org.bfabric.Constants;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.indexer.enums.IndexerEnum;
import org.bfabric.service.EntityService;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.DateUtils;
import org.hibernate.Hibernate;

public class IndexWorker implements Runnable {

    private static final int DOCS_FETCH_SIZE = 10000;

    private static final Logger logger = Logger.getLogger(IndexWorker.class.getName());

    private static final int LUCENE_SORT_FIELD_MAX_SIZE = 2000; // user defined

    private EntityService entityService;

    private IndexMap indexMap;

    private IndexWriter indexWriter;

    public IndexWorker(IndexMap indexMap, IndexWriter indexWriter) {
        entityService = CDI.current().select(EntityService.class).get();
        this.indexMap = indexMap;
        this.indexWriter = indexWriter;
    }

    public IndexWorker() {
    }

    private static void addKeywordField(Document document, String field, Object value, StringBuilder body) {
        if (value instanceof String) {
            String stringValue = value.toString();
            // if the value is a number with only digits, index it as a long
            if (NumberUtils.isCreatable(stringValue)) {
                try {
                    // if the value contains more than digits, parse it as a double
                    if (StringUtils.isNumeric(stringValue)) {
                        addKeywordFieldLong(document, field, stringValue);
                    } else {
                        addKeywordFieldDouble(document, field, stringValue);
                    }
                } catch (NumberFormatException e) {
                    // if the number has a format which includes letters (e.g. 1234L) index it as a string
                    addKeywordFieldString(document, field, stringValue);
                }
            } else {
                addKeywordFieldString(document, field, stringValue);
            }

            if (body != null) {
                body.append(value).append(" ");
            }
        } else if (value instanceof Collection) {
            for (String term : (Collection<String>) value) {
                addKeywordFieldString(document, field, term);

                if (body != null) {
                    body.append(value).append(" ");
                }
            }
        }
    }

    private static void addKeywordFieldDouble(Document document, String field, String stringValue) {
        double value = Double.parseDouble(stringValue);
        document.add(new DoublePoint(field, value));
        document.add(new TextField(field, String.valueOf(value), Field.Store.YES));
        document.add(new SortedDocValuesField(field, new BytesRef(getBytes(value))));
    }

    private static void addKeywordFieldLong(Document document, String field, String stringValue) {
        long value = Long.parseLong(stringValue);
        document.add(new LongPoint(field, value));
        document.add(new TextField(field, String.valueOf(value), Field.Store.YES));
        document.add(new SortedDocValuesField(field, new BytesRef(getBytes(value))));
    }

    private static void addKeywordFieldString(Document document, String field, String stringValue) {
        String value = stringValue.substring(0, Math.min(stringValue.length(), LUCENE_SORT_FIELD_MAX_SIZE));
        document.add(new TextField(field, stringValue, Field.Store.YES));
        document.add(new SortedDocValuesField(field, new BytesRef(value)));
    }

    private static byte[] getBytes(long value) {
        return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(value).array();
    }

    private static byte[] getBytes(double value) {
        return ByteBuffer.allocate(Double.SIZE / Byte.SIZE).putDouble(value).array();
    }

    private void deleteEntities(IndexMapEnum indexMapEnum) {
        if (indexMapEnum != null) {
            BooleanQuery.Builder booleanQueryBuilder;
            try {
                booleanQueryBuilder = new BooleanQuery.Builder();
                booleanQueryBuilder.add(new TermQuery(new Term(IndexMapContentEnum.INDEXMAPTYPE.getField(), indexMapEnum.getType().name().toLowerCase())), BooleanClause.Occur.MUST);

                getIndexWriter().deleteDocuments(booleanQueryBuilder.build());
                getIndexWriter().commit();
                // logger.fine("Removed entities from index: " + indexMapEnum.getType().name());
            } catch (Exception e) {
                logger.severe("Failed to delete class from index: " + e);
            }
        }
    }

    private void deleteEntities(Set<Indexable> entities) {
        if (entities != null && !entities.isEmpty()) {
            BooleanQuery.Builder booleanQueryBuilder;
            try {
                for (Indexable entity : entities) {
                    booleanQueryBuilder = new BooleanQuery.Builder();
                    booleanQueryBuilder.add(new TermQuery(new Term(IndexMapContentEnum.ID.getField(), String.valueOf(entity.getId()))), BooleanClause.Occur.MUST);
                    booleanQueryBuilder.add(new TermQuery(new Term(IndexMapContentEnum.INDEXMAPTYPE.getField(), entity.getIndexMapEnum().getType().name()
                        .toLowerCase())), BooleanClause.Occur.MUST);
                    getIndexWriter().deleteDocuments(booleanQueryBuilder.build());
                }
                getIndexWriter().commit();
                // logger.fine("Removed entities from index: " + entities.size());
            } catch (Exception e) {
                logger.severe("Failed to delete entities from index: " + e);
            }
        }
    }

    public IndexWriter getIndexWriter() {
        if (indexWriter == null || !indexWriter.isOpen()) {
            indexWriter = IndexerEnum.ALL.getIndexer().getIndexWriter();
        }
        return indexWriter;
    }

    private void indexClasses(IndexMapEnum indexMapEnum) {
        if (indexMapEnum != null) {
            String className = ClassHelper.getTrimmedClassName(indexMapEnum.getEntityClass());
            if (className != null) {
                StringBuilder query = new StringBuilder("FROM ");
                query.append(className).append(" entity ");

                // Compose entity list query.
                StringBuilder listQuery = new StringBuilder(query.toString());
                listQuery.insert(0, "SELECT entity ");
                listQuery.append(" ORDER BY entity.id");

                // Compose entity count query.
                StringBuilder countQuery = new StringBuilder(query.toString());
                countQuery.insert(0, "SELECT count(entity) ");

                // Perform and log indexing activity.
                long startTime = System.currentTimeMillis();
                deleteEntities(indexMapEnum);
                long indexedEntriesCount = indexQueryResults(listQuery.toString(), countQuery.toString());
                long elapsedTime = System.currentTimeMillis() - startTime;
                logger.info("Indexed " + indexMapEnum + ": " + indexedEntriesCount + " | " + elapsedTime + "ms" + (indexedEntriesCount > 0 && elapsedTime > 0 ? " -> " + elapsedTime / indexedEntriesCount + "ms/entity" : Constants.EMPTY_STRING));
            }
        }
    }

    private void indexEntities(Collection<? extends Indexable> indexableEntities) {
        // logger.info("Start indexing entities " + indexableEntities.size());
        if (indexableEntities != null && !indexableEntities.isEmpty()) {
            List<IndexMap> indexMaps = new ArrayList<>();
            // Iterate over the chunks and prepare all documents to be indexed.
            for (Indexable entity : indexableEntities) {
                if (entity != null) {
                    IndexMap indexMap = null;
                    try {
                        indexMap = entity.getIndexMap();
                    } catch (Exception e) {
                        try {
                            // If entity is not managed anymore, re-read the entity.
                            Class<? extends Indexable> entityClass = entity.getClass();
                            Long entityId = entity.getId();
                            entity = entityService.find(entityClass, entityId);
                            indexMap = entity.getIndexMap();
                        } catch (Exception e2) {
                            logger.fine("Could not index: " + e2.getMessage());
                        }
                    }
                    if (indexMap != null) {
                        indexMaps.add(indexMap);
                    }
                }
            }
            // Index document.
            indexMaps(indexMaps);
            // Cleanup.
            indexMaps.clear();
        }
        // logger.info("Finished indexing entities " + indexableEntities.size());
    }

    private void indexMaps(Collection<IndexMap> indexMaps) {
        // logger.info("indexMaps: " + indexMaps);
        if (indexMaps != null && !indexMaps.isEmpty()) {
            IndexReader indexReader = null;
            IndexSearcher indexSearcher;
            try {
                indexReader = DirectoryReader.open(getIndexWriter(), true, true);
                indexSearcher = new IndexSearcher(indexReader);

                for (IndexMap indexMap : indexMaps) {
                    boolean newerExists = false;
                    Document document = prepareDocument(indexMap);

                    BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
                    booleanQueryBuilder.add(new TermQuery(new Term(IndexMapContentEnum.ID.getField(), String.valueOf(document.get(IndexMapContentEnum.ID.getField())))), BooleanClause.Occur.MUST);
                    booleanQueryBuilder.add(new TermQuery(new Term(IndexMapContentEnum.INDEXMAPTYPE.getField(), document.get(IndexMapContentEnum.INDEXMAPTYPE.getField())
                        .toLowerCase())), BooleanClause.Occur.MUST);

                    TopScoreDocCollector collector = TopScoreDocCollector.create(10, 10);
                    BooleanQuery booleanQuery = booleanQueryBuilder.build();
                    indexSearcher.search(booleanQuery, collector);
                    ScoreDoc[] hits = collector.topDocs().scoreDocs;

                    if (hits.length > 1) {
                        // If more than 1 document exists, delete them all without checking the modDate
                        logger.warning("More then one match in index for document ID " + document.get(IndexMapContentEnum.ID.getField()) + ", document Type " + document.get(IndexMapContentEnum.INDEXMAPTYPE.getField()) + ". I will delete them all but this is *highly* suspicious.");
                    } else if (hits.length == 1) {
                        Document doc = indexSearcher.doc(hits[0].doc);
                        LocalDateTime indexDate;
                        if (doc.getField(IndexMapContentEnum.MODIFIED.getFieldColSuffix()) != null) {
                            indexDate = DateUtils.getDateTimeFrom(doc.getField(IndexMapContentEnum.MODIFIED.getFieldColSuffix()).stringValue());
                            if (indexDate != null) {
                                IndexMapContent content = (IndexMapContent) indexMap.get(Constants.INDEXMAP_CONTENT);
                                LocalDateTime docDate = DateUtils.getDateTimeFrom((String) content.get(IndexMapContentEnum.MODIFIED.getFieldColSuffix()));
                                if (docDate != null && indexDate.isAfter(docDate)) {
                                    newerExists = true;
                                    logger.warning("Update document [" + document.get(IndexMapContentEnum.ID.getField()) + ", " + document.get(IndexMapContentEnum.INDEXMAPTYPE.getField())
                                        + "] discarded because of existing newer ModDate: [message: " + docDate + ", index: " + indexDate + "]");
                                }
                            }
                        }
                    }

                    // Just update the document if it is older than the object
                    if (!newerExists) {
                        // Delete document(s) from index only if existing
                        if (hits.length > 0) {
                            getIndexWriter().deleteDocuments(booleanQuery);
                        }
                        // Add the document to the index
                        getIndexWriter().addDocument(document);
                    }
                }
                getIndexWriter().commit();
                indexReader.close();
            } catch (CorruptIndexException e) {
                logger.severe("The Lucene Index is (partially) corrupted!");
                e.printStackTrace();
            } catch (Exception e) {
                logger.severe("There was a problem when reading/writing to Lucene Index.");
                e.printStackTrace();
            } finally {
                if (indexReader != null) {
                    try {
                        indexReader.close();
                    } catch (IOException e2) {
                        logger.severe(e2.getMessage() + ": " + e2);
                    }
                }
            }
        }
    }

    private long indexQueryResults(String query, String queryCount) {
        int currentPosition = 0;
        long indexedEntitiesCount = 0;
        List<? extends Indexable> currentEntityList;
        try {
            // Iterate over all entities which are to be indexed.
            //Long numberOfEntities = Math.min(entityService.getQuerySingleResult(queryCount), DOCS_FETCH_SIZE);
            Long numberOfEntities = entityService.getQuerySingleResult(queryCount);
            long queryTime = 0;
            long indexTime = 0;
            while (currentPosition < numberOfEntities) {
                long startQueryTime = System.currentTimeMillis();
                currentEntityList = (List<? extends Indexable>) entityService.getQueryResultList(query, currentPosition, DOCS_FETCH_SIZE);
                long currentQueryTime = System.currentTimeMillis() - startQueryTime;
                if (currentQueryTime > 5000) {
                    logger.fine("High Query Time: " + currentQueryTime + "ms");
                }
                queryTime += System.currentTimeMillis() - startQueryTime;
                long startIndexTime = System.currentTimeMillis();
                currentPosition += DOCS_FETCH_SIZE;
                indexedEntitiesCount += currentEntityList.size();
                // Perform and log indexing activity.
                if (!currentEntityList.isEmpty()) {
                    indexEntities(currentEntityList);
                    // Add log message depending on whether the list contains one or more entities to be indexed.
                    logger.fine("[" + indexedEntitiesCount + "/" + numberOfEntities + "] " + currentEntityList.get(0).getIndexMapEnum() + " " + "indexed");
                } else {
                    logger.fine("[" + indexedEntitiesCount + "/" + numberOfEntities + "] " + query + " 0 indexed");
                }
                indexTime += System.currentTimeMillis() - startIndexTime;
            }
            logger.fine("[QueryTime=" + queryTime + "/IndexTime=" + indexTime + "] " + query);
            return numberOfEntities;
        } catch (RuntimeException e) {
            logger.severe("Failed to query/index results: " + e.getMessage());
            return 0L;
        }
    }

    @Transactional
    public Document prepareDocument(final IndexMap indexMap) {
        Document document = new Document();
        StringBuilder body = new StringBuilder();

        // Add main keywords
        final String id = (String) indexMap.get(Constants.INDEXMAP_ID);
        addKeywordField(document, IndexMapContentEnum.ID.getField(), id, body);

        final String type = (String) indexMap.get(Constants.INDEXMAP_TYPE);
        addKeywordField(document, IndexMapContentEnum.INDEXMAPTYPE.getField(), type, null);

        final LocalDateTime modified = (LocalDateTime) indexMap.get(Constants.INDEXMAP_MODIFIED);
        addKeywordField(document, IndexMapContentEnum.MODIFIED.getField(), DateUtils.getDateIndexString(modified), null);
        addKeywordField(document, IndexMapContentEnum.MODIFIED.getFieldColSuffix(), DateUtils.getDateAsFormattedString(modified), null);

        final String modifiedBy = (String) indexMap.get(Constants.INDEXMAP_MODIFIEDBY);
        addKeywordField(document, IndexMapContentEnum.MODIFIEDBY.getField(), modifiedBy, null);

        final LocalDateTime created = (LocalDateTime) indexMap.get(Constants.INDEXMAP_CREATED);
        addKeywordField(document, IndexMapContentEnum.CREATED.getField(), DateUtils.getDateIndexString(created), null);
        addKeywordField(document, IndexMapContentEnum.CREATED.getFieldColSuffix(), DateUtils.getDateAsFormattedString(created), null);

        final String createdBy = (String) indexMap.get(Constants.INDEXMAP_CREATEDBY);
        addKeywordField(document, IndexMapContentEnum.CREATEDBY.getField(), createdBy, null);

        // Add filter keywords
        final String status = (String) indexMap.get(Constants.INDEXMAP_STATUS);
        if (status != null) {
            addKeywordField(document, IndexMapContentEnum.PERM_STATUS.getField(), status, null);
        }

        final LocalDate doiCreated = (LocalDate) indexMap.get(Constants.INDEXMAP_DOI_CREATED);
        if (doiCreated != null) {
            addKeywordField(document, IndexMapContentEnum.PERM_DOI.getField(), "true", null);
        }

        final String group = indexMap.get(Constants.INDEXMAP_GROUP) instanceof RoleEnum ? ((RoleEnum) indexMap.get(Constants.INDEXMAP_GROUP)).getName() : (String) indexMap.get(Constants.INDEXMAP_GROUP);
        if (group != null) {
            addKeywordField(document, IndexMapContentEnum.PERM_GROUP.getField(), group, null);
        }

        // Add additional keywords
        final IndexMapContent content = (IndexMapContent) indexMap.get(Constants.INDEXMAP_CONTENT);
        for (final Map.Entry<String, Object> entry : content.entrySet()) {
            final String field = entry.getKey();
            final Object value = entry.getValue();
            if (field != null && value != null) {
                if (value instanceof LocalDate) {
                    addKeywordField(document, field, DateUtils.getDateIndexString((LocalDate) value), body);
                    addKeywordField(document, field + Constants.INDEXER_COL_SUFFIX, DateUtils.getDateAsFormattedString((LocalDate) value), body);
                } else if (value instanceof LocalDateTime) {
                    addKeywordField(document, field, DateUtils.getDateIndexString((LocalDateTime) value), body);
                    addKeywordField(document, field + Constants.INDEXER_COL_SUFFIX, DateUtils.getDateAsFormattedString((LocalDateTime) value), body);
                } else if (value instanceof AbstractEntity) {
                    addKeywordField(document, field, Hibernate.unproxy(value).toString(), body);
                } else {
                    addKeywordField(document, field, value.toString(), body);
                }
            }
        }
        document.add(new TextField(IndexMapContentEnum.BODY.getField(), body.toString(), Field.Store.NO));
        document.add(new SortedDocValuesField(IndexMapContentEnum.BODY.getField(), new BytesRef(IndexMapContentEnum.BODY.getField())));

        return document;
    }

    @Override
    public synchronized void run() {
        try {
            if (indexMap.containsKey(Constants.INDEX_DELETE_ENTITIES)) {
                deleteEntities((Set<Indexable>) indexMap.get(Constants.INDEX_DELETE_ENTITIES));
            } else if (indexMap.containsKey(Constants.INDEX_CLASS)) {
                indexClasses((IndexMapEnum) indexMap.get(Constants.INDEX_CLASS));
            } else if (indexMap.containsKey(Constants.INDEX_ENTITIES)) {
                wait(2000);
                Integer indexableEntitiesCount = (Integer) indexMap.get(Constants.INDEX_ENTITIES_COUNT);
                if (indexableEntitiesCount != null) {
                    wait(Math.min(indexableEntitiesCount, 8000));
                }
                indexEntities((Collection<? extends Indexable>) indexMap.get(Constants.INDEX_ENTITIES));
            } else if (indexMap.containsKey(Constants.INDEX_QUERY)) {
                wait(2000);
                indexQueryResults((String) indexMap.get(Constants.INDEX_QUERY), (String) indexMap.get(Constants.INDEX_QUERY_COUNT));
            } else {
                throw new Exception("Incorrect properties set in message.");
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
    }

    public void setIndexWriter(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
    }
}