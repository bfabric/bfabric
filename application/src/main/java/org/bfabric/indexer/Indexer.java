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

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import net.sf.ehcache.util.FindBugsSuppressWarnings;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.bfabric.Constants;
import org.bfabric.entity.Configuration;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.util.ConfigurationHelper;

public class Indexer {

    private static final Logger logger = Logger.getLogger(Indexer.class.getName());

    // Configure thread pool for indexing: reserve two processors for non-indexing tasks.
    // Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() - 2, 1));
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private IndexWriter indexWriter;

    public Indexer() {
    }

    private void executeIndexing(final IndexMap indexMap) {
        if (getIndexWriter() != null && getIndexWriter().isOpen()) {
            executorService.execute(new IndexWorker(indexMap, getIndexWriter())); // execute asynchronously.
        } else {
            logger.fine("IndexWriter not open!");
        }
    }

    public File getIndexDirectory() {
        File indexDirectory = null;
        try {
            Configuration configuration = ConfigurationHelper.getConfiguration();
            if (configuration != null && configuration.getIndexPath() != null) {
                indexDirectory = new File(configuration.getIndexPath());
            }
            return indexDirectory;
        } catch (Exception e) {
            logger.fine("IndexDirectory could not be created!");
            return null;
        }
    }

    public IndexWriter getIndexWriter() {
        if (indexWriter == null || !indexWriter.isOpen()) {
            setIndexWriter();
        }
        return indexWriter;
    }

    public void indexClass(Class<? extends Indexable> clazz) {
        indexClass(IndexMapEnum.getEnum(clazz));
    }

    public void indexClass(final IndexMapEnum indexMapEnum) {
        IndexMap indexMap = new IndexMap();
        indexMap.put(Constants.INDEX_CLASS, indexMapEnum);
        executeIndexing(indexMap);
    }

    public void indexClasses() {
        // Clear queued tasks.
        ((ThreadPoolExecutor) executorService).getQueue().clear();
        for (IndexMapEnum indexMapEnum : IndexMapEnum.values()) {
            // Create a new request for each class.
            indexClass(indexMapEnum);
        }
    }

    public void indexEntities(Collection<? extends Indexable> indexableEntities) {
        if (indexableEntities != null && !indexableEntities.isEmpty()) {
            final IndexMap indexMap = new IndexMap();
            indexMap.put(Constants.INDEX_ENTITIES, indexableEntities);
            indexMap.put(Constants.INDEX_ENTITIES_COUNT, indexableEntities.size());
            executeIndexing(indexMap);
        }
    }

    public void indexQueryResult(String query, String queryCount) {
        if (query != null && queryCount != null) {
            final IndexMap indexMap = new IndexMap();
            indexMap.put(Constants.INDEX_QUERY, query);
            indexMap.put(Constants.INDEX_QUERY_COUNT, queryCount);
            executeIndexing(indexMap);
        }
    }

    public void removeFromIndex(Collection<? extends Indexable> entities) {
        final IndexMap indexMap = new IndexMap();
        indexMap.put(Constants.INDEX_DELETE_ENTITIES, entities);
        executeIndexing(indexMap);
    }

    @FindBugsSuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void setIndexWriter() {
        if (indexWriter == null || !indexWriter.isOpen()) {
            try {
                File indexDirectory = getIndexDirectory();
                // Get index directory and create if it does not already exist.
                if (indexDirectory != null && (indexDirectory.exists() || indexDirectory.mkdirs())) {
                    // Avoid access to index directory from multiple index writer concurrently.
                    File lock = new File(indexDirectory, IndexWriter.WRITE_LOCK_NAME);
                    //noinspection ResultOfMethodCallIgnored
                    lock.createNewFile();
                    if (lock.delete()) {
                        // Use this method to instantiate the subclass that fits best (SimpleFSDirectory, NIOFSDirectory, ...).
                        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(new StandardAnalyzer());
                        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                        indexWriterConfig.setRAMBufferSizeMB(512);
                        indexWriterConfig.setUseCompoundFile(false);
                        indexWriter = new IndexWriter(FSDirectory.open(indexDirectory.toPath()), indexWriterConfig);
                    }
                }
            } catch (Exception e) {
                logger.severe("Cannot create the IndexWriter. " + e);
            }
        }
    }
}