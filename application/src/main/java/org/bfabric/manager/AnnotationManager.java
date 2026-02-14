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

package org.bfabric.manager;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Annotation;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.AnnotationService;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class AnnotationManager extends AbstractEntityManager<Annotation> {

    private static final long serialVersionUID = 1;

    @Inject
    private AnnotationService annotationService;

    @Param
    private String annotationType;

    @Param
    private String annotationTypeFilter;

    private Annotation mergeSelection = new Annotation();

    private Annotation merged;

    private String sampleType;

    public AnnotationManager() {
        super(Annotation.class);
    }

    @Override
    protected Annotation createInstance() {
        final Annotation annotation = super.createInstance();
        if (annotation != null) {
            annotation.setType(annotationType);
        }
        return annotation;
    }

    @Produces
    @Named("annotation")
    public Annotation getAnnotation() {
        return getInstance();
    }

    public String getAnnotationTypeFilter() {
        return annotationTypeFilter;
    }

    public List<SampleAttributeEnum> getAnnotationTypes() {
        return SampleAttributeEnum.getAnnotationTypes();
    }

    public Annotation getMergeSelection() {
        return mergeSelection;
    }

    public Annotation getMerged() {
        return merged;
    }

    public String getSampleType() {
        return sampleType;
    }

    public List<Annotation> getSimilarAnnotations() {
        return getSimilarAnnotations(getIdLong());
    }

    @CachedMethodResult
    public List<Annotation> getSimilarAnnotations(Long annotationId) {
        return annotationService.getSimilarAnnotationsById(annotationId);
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        initMerge();
    }

    public void initMerge() {
        if (getInstance() != null && mergeId != null) {
            try {
                merged = getInstance(mergeId);
                if (merged != null) {
                    mergeSelection = getAnnotation().clone();
                    if (StringHelper.isEmpty(getMergeSelection().getDescription())) {
                        mergeSelection.setDescription(getMerged().getDescription());
                    }
                } else {
                    redirectToEntityNotFoundErrorPage(getEntityClass().getSimpleName(), String.valueOf(mergeId));
                }
            } catch (NumberFormatException e) {
                redirectToEntityIdInvalidErrorPage(getEntityClass().getSimpleName(), mergeId);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String merge() {
        try {
            annotationService.merge(getAnnotation(), getMerged(), getMergeSelection());
            bufferMergeSuccessMessage();
            return getShowScreenRedirectURL();
        } catch (final Exception e) {
            mergeFailed(e);
        }
        return null;
    }

    public String release() {
        String ret = null;
        if (isManaged()) {
            getAnnotation().setReleased(true);
            ret = save();
        }
        return ret;
    }

    @Override
    public String save() {
        return validateAndSave(annotationService);
    }

    public void setAnnotationTypeFilter(String annotationTypeFilter) {
        this.annotationTypeFilter = annotationTypeFilter;
    }

    public void setMergeSelection(Annotation mergeSelection) {
        this.mergeSelection = mergeSelection;
    }

    public void setMerged(Annotation annotation) {
        merged = annotation;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }
}
