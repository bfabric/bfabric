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

package org.bfabric.entity;

import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.util.StringHelper;

@Entity
@XmlRootElement
public class Criterion extends AbstractBaseEntity implements NotEntityLoggable {

    private static final Logger logger = Logger.getLogger(Criterion.class.getName());

    private static final long serialVersionUID = 1;

    @Column(name = "fieldName")
    @NotBlank
    @Size(max = 128)
    @XmlElement
    protected String field;

    @Column(name = "matchName")
    @Size(max = 16)
    @XmlElement
    protected String match;

    @NotBlank
    @Size(max = 1024)
    @XmlElement
    protected String term;

    @ManyToOne
    @JoinColumn(name = "queryid")
    @NotNull
    @XmlIDREF
    private Query query;

    public Criterion() {
        super();
        field = IndexMapContentEnum.ANY.getField();
    }

    public Criterion(String field, String term) {
        super();
        this.field = field != null ? field : IndexMapContentEnum.ANY.getField();
        this.term = term;
    }

    @Override
    public Criterion clone() throws CloneNotSupportedException {
        return (Criterion) super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Criterion)) {
            return false;
        }

        final Criterion other = (Criterion) obj;

        return getTerm() != null && getTerm().contains(other.getTerm()) && getField() != null && getField().equals(other.getField());
    }

    public org.apache.lucene.search.Query generateQuery(Analyzer analyzer) {
        String queryTerm = this.term;
        String queryField = this.field;

        // when the term is * this criterion matches all Documents
        if (queryTerm.matches("\\s*\\*\\s*")) {
            return new MatchAllDocsQuery();
        }

        // Specify to search in any field.
        if (IndexMapContentEnum.ANY.getField().equals(queryField)) {
            queryField = IndexMapContentEnum.BODY.getField();
        }

        // Create a query - wild card allowed.
        QueryParser queryParser = new QueryParser(queryField, analyzer);
        queryParser.setAllowLeadingWildcard(true);

        org.apache.lucene.search.Query generatedQuery = null;
        try {
            generatedQuery = queryParser.parse(queryTerm);
        } catch (ParseException e) {
            logger.severe(e.toString());
        }

        return generatedQuery;
    }

    public String getField() {
        return this.field;
    }

    public String getMatch() {
        return this.match;
    }

    public Query getQuery() {
        return query;
    }

    public String getTerm() {
        return this.term;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + 31 * term.hashCode() + 31 * field.hashCode();
    }

    public void setField(String field) {
        this.field = StringHelper.format(field);
    }

    public void setMatch(String match) {
        this.match = StringHelper.format(match);
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public void setTerm(String term) {
        this.term = StringHelper.format(term);
    }
}
