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

package org.bfabric.service.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.bfabric.Messages;
import org.bfabric.entity.Query;
import org.bfabric.manager.QueryManager;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

public class QueryTableModel<E> extends BfabricLazyDataModel<E> {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(QueryTableModel.class.getName());

    private QueryManager queryManager;

    public QueryTableModel(QueryManager queryManager) {
        super();
        this.queryManager = queryManager;
    }

    public QueryManager getQueryManager() {
        return queryManager;
    }

    @Override
    public int getSize() {
        return getQueryManager().getTotalHits();
    }

    public List<HashMap<String, Object>> getWrapperData(int numberOfHits) {
        // Create result list
        List<HashMap<String, Object>> results = new ArrayList<>();
        try {
            ScoreDoc[] resultHits = getQueryManager().getHits(numberOfHits).scoreDocs;
            IndexSearcher searcher = getQueryManager().getIndexSearcher();
            for (int i = 0; i < getRowCount(); i++) {
                final HashMap<String, Object> hit = new HashMap<>();
                int docId = resultHits[i].doc;
                final Document doc = searcher.doc(docId);
                for (IndexableField field : doc.getFields()) {
                    hit.put(field.name(), field.stringValue());
                }
                results.add(hit);
            }
        } catch (Exception e) {
            logger.severe(Messages.get("failedToCreateSearchResult") + ": " + e);
        }

        return results;
    }

    @Override
    public List<E> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        // fix sort
        if (sortBy != null && !sortBy.isEmpty()) {
            SortMeta sortMeta = sortBy.entrySet().iterator().next().getValue();
            if (sortMeta != null) {
                getQueryManager().getQuery().setSortColumn(sortMeta.getField());
                if (sortMeta.getOrder() == SortOrder.ASCENDING) {
                    getQueryManager().getQuery().setSortDirection(Query.ASCENDING);
                } else if (sortMeta.getOrder() == SortOrder.DESCENDING) {
                    getQueryManager().getQuery().setSortDirection(Query.DESCENDING);
                }
            }
        }
        getQueryManager().search(pageSize);

        // rowCount
        setRowCount((int) getQueryManager().getQuery().getTotal());
        this.lastPageSize = pageSize;

        Map<Integer, Map<String, Object>> results = getQueryManager().findLuceneResults(first, pageSize);
        dataList = (List<E>) new ArrayList<>(results.values());

        return dataList;
    }

    public void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
    }
}
