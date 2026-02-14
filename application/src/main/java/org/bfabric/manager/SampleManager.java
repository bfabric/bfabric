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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.Min;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Container;
import org.bfabric.entity.Project;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SampleType;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.enums.SampleFormEnum;
import org.bfabric.enums.SampleQCTypeEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.AnnotationService;
import org.bfabric.service.InstrumentService;
import org.bfabric.service.MultiplexKitService;
import org.bfabric.service.SamplePreparationProtocolService;
import org.bfabric.service.SampleService;
import org.bfabric.util.AJAX;
import org.bfabric.util.SampleAttributeHelper;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class SampleManager extends AbstractContainerDependentEntityManager<Sample> {

    private static final long serialVersionUID = 1;

    private final Set<Sample> initialParentsOfMultiplexIdAssignmentSample = new HashSet<>();

    private Annotation annotation;

    @Inject
    private AnnotationService annotationService;

    @Min(2)
    private int fractions = 2;

    private Container fractionsContainer;

    private SampleType fractionsSampleType;

    private Map<Integer, Integer> initialSampleIdentifiersNumberMap = new HashMap<>();

    @Param
    private String move;

    private Sample multiplexIdAssignmentSample;

    @Param
    private Long parentSampleId;

    @Param
    private Long replacementSampleId;

    @Inject
    private SampleAttributeHelper sampleAttributeHelper;

    private Map<Integer, Integer> sampleIdentifiersNumberMap = new HashMap<>();

    @Inject
    private SampleService sampleService;

    public SampleManager() {
        super(Sample.class);
    }

    public void assignMultiplexIds() {
        getMultiplexIdAssignmentSample().recomputeParentSamplesOfUserMultiplex();
        resetAssignMultiplexIdsModalPanel();
        AJAX.update(Constants.EDIT + ":" + Constants.MULTIPLEXED + Constants.TRI_STATE_CHECKBOX_GROUP);
    }

    public void cancelAssignMultiplexIds() {
        getMultiplexIdAssignmentSample().resetMultiplexIdAssignmentSample(getInitialParentsOfMultiplexIdAssignmentSample());
        getInitialParentsOfMultiplexIdAssignmentSample().clear();
        setSampleIdentifiersNumberMap(new HashMap<>(getInitialSampleIdentifiersNumberMap()));
        getInitialSampleIdentifiersNumberMap().clear();
        resetAssignMultiplexIdsModalPanel();
    }

    public void createAnnotation(SampleAttributeEnum sampleAttributeEnum) {
        setAnnotation(new Annotation());
        getAnnotation().setType(sampleAttributeEnum.getLabel());
    }

    @Override
    protected Sample createInstance() {
        Sample sample = super.createInstance();

        if (getParentSampleId() != null) {
            final Sample parentSample = entityService.find(Sample.class, getParentSampleId());
            sample.setContainer(parentSample.getContainer());
            sample.setSampleType(parentSample.getSampleType());
            sample.setName(parentSample.getName());
            sample.getParents().add(parentSample);
        }
        if (getContainerId() != null) {
            sample.setContainer(entityService.find(Container.class, getContainerId()));
        }
        if (getReplacementSampleId() != null) {
            final Sample replacementSample = entityService.find(Sample.class, getReplacementSampleId());
            if (replacementSample != null && replacementSample.isUserDecisionRequired()) {
                try {
                    sample = replacementSample.clone();
                } catch (CloneNotSupportedException e) {
                    sample.setContainer(replacementSample.getContainer());
                    sample.setSampleType(replacementSample.getSampleType());
                    sample.setName(replacementSample.getName());
                }
                sample.setName(sample.getName() + "_" + Messages.get("replacement"));
                sample.setReplaces(replacementSample);
            }
        }

        return sample;
    }

    public String fractionate() {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        if (getFractionsSampleType() == null) {
            validationErrorMsg.put("fractionate:fractionsSampleType", Messages.get("required"));
        }
        if (!validationErrorMsg.isEmpty()) {
            getFacesMessagesManager().printValidationErrors(validationErrorMsg);
            return null;
        }
        final Map<String, String> fParams = new HashMap<>();
        fParams.put("fractionsParentId", getSample().getIdString());
        fParams.put("fractions", String.valueOf(getFractions()));
        fParams.put("fractionsSampleTypeId", getFractionsSampleType().getIdString());
        fParams.put("containerId", getFractionsContainer().getIdString());
        return createRedirectURL("sample/edit-batch", null, null, fParams);
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public Long getContainerId() {
        return containerId;
    }

    public List<Container> getExtensibleReadableContainersExcluding(String filterString) {
        Container oldContainer = null;
        if (getSample().getOldContainerId() != 0) {
            oldContainer = containerService.find(Container.class, getSample().getOldContainerId());
        }
        return containerService.getExtensibleReadableContainersExcluding(filterString, oldContainer, identityManager.getCurrentUser());
    }

    public int getFractions() {
        return fractions;
    }

    public Container getFractionsContainer() {
        return fractionsContainer;
    }

    public SampleType getFractionsSampleType() {
        return fractionsSampleType;
    }

    public Set<Sample> getInitialParentsOfMultiplexIdAssignmentSample() {
        return initialParentsOfMultiplexIdAssignmentSample;
    }

    public Map<Integer, Integer> getInitialSampleIdentifiersNumberMap() {
        return initialSampleIdentifiersNumberMap;
    }

    public String getMove() {
        return move;
    }

    public Sample getMultiplexIdAssignmentSample() {
        return multiplexIdAssignmentSample;
    }

    public Long getParentSampleId() {
        return parentSampleId;
    }

    public List<Sample> getPossibleChildren(String filterString) {
        final List<Sample> samples = getSample().getClone().getChildrenFiltered(filterString);
        samples.removeAll(getSample().getChildren());
        return samples;
    }

    public List<Sample> getPossibleParents(String filterString) {
        if (getSample().isCloned()) {
            final List<Sample> possibleParentsForCloning = getSample().getClone().getParentsFiltered(filterString);
            possibleParentsForCloning.removeAll(getSample().getParents());
            return possibleParentsForCloning;
        }
        List<Sample> exclude = new ArrayList<>();
        if (getSample().isManaged()) {
            exclude.add(getSample());
        }
        for (Sample parentSample : getSample().getParents()) {
            if (!parentSample.isUserSampleInMultiplexType() || parentSample.isManaged()) {
                exclude.add(parentSample);
            }
        }
        exclude.addAll(getSample().getDescendants());
        return sampleService.getSamplesByContainer(filterString, getSample().getContainer(), exclude);
    }

    public List<Sample> getPotentialReplacesFiltered(String filterString) {
        return sampleService.getPotentialReplacesFiltered(filterString, getSample());
    }

    @Override
    public String getRedirectURLAfterCancelCreated() {
        return getSample().isClonedOrMoved() ? createRedirectShowScreenURL(Sample.class.getSimpleName(), getClonedId(), null, null) : getRedirectURLFromRefererUrl();
    }

    public Long getReplacementSampleId() {
        return replacementSampleId;
    }

    @Produces
    @Named("sample")
    public Sample getSample() {
        return getInstance();
    }

    @CachedMethodResult
    public List<SampleAttributeEnum> getSampleAttributeEnumsEditable(Sample aSample, SampleType sampleType, SampleFormEnum sampleFormEnum, SampleQCTypeEnum sampleQCTypeEnum) {
        // Note: Do not eliminate this method as it is necessary and used solely for caching purposes.
        if (getSample().isSampleTypeSpecificAttributesEditable()) {
            List<SampleAttributeEnum> sampleAttributeEnums = SampleAttributeEnum
                .getAttributeEnums(true, aSample, sampleType != null ? sampleType.getName() : null, true, sampleFormEnum, true, sampleQCTypeEnum, false, false);
            // Remove the tubeId for editing if the sample was created before the bfabric10 release date.
            if (aSample.isCreatedBeforeBfabric10ReleaseDate()) {
                sampleAttributeEnums.remove(SampleAttributeEnum.TUBE_ID);
            }
            // Remove the sizeGenomeEstimated if it is not to be rendered.
            if (sampleAttributeEnums.contains(SampleAttributeEnum.SIZE_GENOME_ESTIMATED) && !sampleAttributeHelper
                .isSizeGenomeEstimatedAndRendered(SampleAttributeEnum.SIZE_GENOME_ESTIMATED, aSample.getContainer(), sampleType != null ? sampleType.getName() : null)) {
                sampleAttributeEnums.remove(SampleAttributeEnum.SIZE_GENOME_ESTIMATED);
            }
            return sampleAttributeEnums;
        }
        return Collections.singletonList(SampleAttributeEnum.MOLARITY);
    }

    public Map<Integer, Integer> getSampleIdentifiersNumberMap() {
        return sampleIdentifiersNumberMap;
    }

    public List<?> getSelectionValuesBySampleAttributeFiltered(String filterString) {
        final FacesContext context = FacesContext.getCurrentInstance();
        final List<?> emptyList = new ArrayList<>();
        if (context != null) {
            final SampleAttributeEnum sampleAttributeEnum = SampleAttributeEnum.getAttributeByName((String) UIComponent.getCurrentComponent(context).getAttributes().get("sampleAttributeEnum"));
            if (sampleAttributeEnum != null) {
                if (sampleAttributeEnum.isAnnotationType()) {
                    return annotationService.getAnnotationsByAttributeType(filterString, getSample(), sampleAttributeEnum);
                }
                if (SampleAttributeEnum.SAMPLE_PREPARATION_PROTOCOL.equals(sampleAttributeEnum)) {
                    return CDI.current().select(SamplePreparationProtocolService.class).get().getFilteredEnabledSamplePreparationProtocolsIncluding(filterString, getSample());
                }
                if (SampleAttributeEnum.MULTIPLEX_KIT.equals(sampleAttributeEnum) || SampleAttributeEnum.MULTIPLEX_KIT_2.equals(sampleAttributeEnum)) {
                    return CDI.current().select(MultiplexKitService.class).get()
                        .getFilteredEnabledIncludingOrderBy(SampleAttributeEnum.MULTIPLEX_KIT.equals(sampleAttributeEnum) ? getSample().getMultiplexKit() : getSample()
                            .getMultiplexKit2(), filterString, null);
                }
                if (SampleAttributeEnum.INSTRUMENT.equals(sampleAttributeEnum)) {
                    return CDI.current().select(InstrumentService.class).get().getFilteredEnabledIncludingOrderBy(getSample().getInstrument(), filterString, null);
                }
            }
            return emptyList;
        }
        return emptyList;
    }

    @CachedMethodResult
    public List<?> getSelectionValuesBySampleAttributeIncluding(SampleAttributeEnum aSampleAttributeEnum, Sample sample, String type) {
        // Important: Do not remove the type parameter as it is used for the @CachedMethodResult annotation.
        if (aSampleAttributeEnum != null && aSampleAttributeEnum.isEnumType()) {
            return aSampleAttributeEnum.getEnumSelectionValuesIncluding(sample);
        }
        return new ArrayList<>();
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getSample() != null) {
            // Make sure the oldSampleAttributeEnums are initialized.
            getSample().setOldSampleAttributeEnums();
            if (getSample().isManaged() && getSample().isInitialParentSamplesOfUserMultiplexInitializationPermitted()) {
                getSample().initializeInitialParentSamplesOfUserMultiplex();
            }
            if (getSample().getMultiplexedByUser() != null && getSample().getMultiplexedByUser()) {
                getSampleIdentifiersNumberMap().put(getSample().hashCode(), getSample().getInitialParentSamplesOfUserMultiplex().size());
            }
            if (getMove() != null) {
                getSample().initMove();
            }
        }
    }

    public void numberOfSamplesInMultiplexChanged(ValueChangeEvent event) {
        if (getMultiplexIdAssignmentSample() != null) {
            getMultiplexIdAssignmentSample().changeNumberOfSamplesInMultiplex(event);
        }
    }

    public void prepareMultiplexIdsModalPanel() {
        resetAssignMultiplexIdsModalPanel();
        getSample().recomputeParentSamplesOfUserMultiplex();
        // Keep the initial values in case of cancel.
        for (Sample parentSample : getSample().getParentSamplesOfUserMultiplex()) {
            parentSample.setOldMultiplexIdsToCurrent();
            parentSample.setOldNamePrefixToCurrent();
            parentSample.setOldUserSampleInMultiplexNameToCurrent();
        }
        getInitialParentsOfMultiplexIdAssignmentSample().addAll(getSample().getParents());
        setInitialSampleIdentifiersNumberMap(new HashMap<>(getSampleIdentifiersNumberMap()));
        if (getSample().getParentSamplesOfUserMultiplex().isEmpty()) {
            // Create a parent sample as no parent sample(s) exist yet.
            getSample().createTemporaryParentOfUserSampleInMultiplex(getSample().createNamePrefixForParentOfUserSampleInMultiplex(1, 1));
            getSample().recomputeParentSamplesOfUserMultiplex();
        }
        int sampleHashCode = getSample().hashCode();
        if (!getSampleIdentifiersNumberMap().containsKey(sampleHashCode)) {
            // Not already marked as multiplexed and used for the multiplex id assigment.
            getSampleIdentifiersNumberMap().put(sampleHashCode, getSample().getParentSamplesOfUserMultiplex().size());
        }
        // Adapt all the names based on the name or the tube id of the multiplexed sample with the correct padding, so they can be ordered by their names.
        int parentSamplesOfUserMultiplexSize = getSample().getParentSamplesOfUserMultiplex().size();
        for (int i = 0; i < parentSamplesOfUserMultiplexSize; i++) {
            getSample().getParentSamplesOfUserMultiplex().get(i).setName(getSample().createNamePrefixForParentOfUserSampleInMultiplex(i + 1, parentSamplesOfUserMultiplexSize));
        }
        setMultiplexIdAssignmentSample(getSample());
    }

    @Override
    public String remove() {
        final String sampleName = getSample().toString();
        boolean hasDescendants = !getSample().getDescendants().isEmpty();
        sampleService.remove(getSample());
        final StringBuilder msg = new StringBuilder(Messages.get("successfullyRemoved") + " " + sampleName);
        if (hasDescendants) {
            msg.append(" ").append(Messages.get("withItsChildSamples"));
        }
        getFacesMessagesManager().bufferWarningClear(msg.toString());
        return getRedirectURLAfterRemove();
    }

    public String removeAllDeletableSamples(Project project) {
        Integer deletedSamplesSize = sampleService.removeAllDeletableSamples(project);
        if (deletedSamplesSize != null) {
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyRemovedSample").replace("{0}", deletedSamplesSize.toString()));
            return createRedirectShowScreenURL(project, "samples", null);
        }
        return null;
    }

    private void resetAssignMultiplexIdsModalPanel() {
        setMultiplexIdAssignmentSample(null);
    }

    public void resetFractionateModalDialog() {
        setFractions(2);
        setFractionsSampleType(null);
        setFractionsContainer(getContextContainer());
    }

    @Override
    public String save() {
        getSample().resetFields();
        if (getSample().getMultiplexedByUser() != null && getSample().getMultiplexedByUser()) {
            // Remove all parents samples which are not of type 'User Library in Pool' as a multiplexed sample can only have parents of type 'User Library in Pool'.
            getSample().getParents().removeIf(parentSample -> !parentSample.isUserSampleInMultiplexType());
            // Remove parent samples of the multiplexed sample which are not in the multiplex anymore.
            getSample().getParents()
                .removeIf(parentSample -> parentSample.isUserSampleInMultiplexType() && getSample().getInitialParentSamplesOfUserMultiplex().contains(parentSample) && !getSample()
                    .getParentSamplesOfUserMultiplex().contains(parentSample));
            getSample().getParents().addAll(getSample().getParentSamplesOfUserMultiplex());
            getSample().recomputeParentSamplesOfUserMultiplex();
        }
        LinkedHashMap<String, String> validationErrorMsg = sampleService.isValid(getSample());
        if (validationErrorMsg.isEmpty()) {
            setCreated(!isManaged());
            if (getSample().getSamplePreparationProtocol() == null) {
                getSample().presetSamplePreparationProtocolFromParentIfEligible();
            }
            sampleService.save(getSample());
            if (getSample().getReplaces() != null) {
                sampleService.save(getSample().getReplaces());
            }
            // Set the context container.
            getContextManager().setContextContainer(getSample().getContainer());
            if (getSample().isMoved()) {
                getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyMoved"));
            } else if (getSample().isCloned()) {
                getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCloned"));
            }
            return postSave(!getSample().isClonedOrMoved(), false);
        }
        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public void saveAnnotation() {
        if (!annotationService.checkUniqueName(getAnnotation())) {
            getFacesMessagesManager().validationError("annotationName", Messages.get("nameNotUniqueForTypeException").replace("{0}", getAnnotation().getType()));
            FacesContext.getCurrentInstance().validationFailed();
        } else {
            entityService.persist(getAnnotation());
            getSample().setAnnotation(getAnnotation());
            setAnnotation(null);
        }
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public void setFractions(int fractions) {
        this.fractions = fractions;
    }

    public void setFractionsContainer(Container fractionsContainer) {
        this.fractionsContainer = fractionsContainer;
    }

    public void setFractionsSampleType(SampleType fractionsSampleType) {
        this.fractionsSampleType = fractionsSampleType;
    }

    public void setInitialSampleIdentifiersNumberMap(Map<Integer, Integer> initialSampleIdentifiersNumberMap) {
        this.initialSampleIdentifiersNumberMap = initialSampleIdentifiersNumberMap;
    }

    public void setMove(String move) {
        this.move = move;
    }

    public void setMultiplexIdAssignmentSample(Sample multiplexIdAssignmentSample) {
        this.multiplexIdAssignmentSample = multiplexIdAssignmentSample;
    }

    public void setParentSampleId(Long parentSampleId) {
        this.parentSampleId = parentSampleId;
    }

    public void setReplacementSampleId(Long replacementSampleId) {
        this.replacementSampleId = replacementSampleId;
    }

    public void setSampleIdentifiersNumberMap(Map<Integer, Integer> sampleIdentifiersNumberMap) {
        this.sampleIdentifiersNumberMap = sampleIdentifiersNumberMap;
    }

    public String userDecisionRequired(Sample sample, Container containerToBeRendered) {
        sampleService.userDecisionRequired(sample, containerToBeRendered);
        return getShowScreenRedirectURL();
    }
}