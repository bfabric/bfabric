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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.list.TechnologyList;
import org.bfabric.service.AnnotationService;
import org.bfabric.service.InstrumentService;
import org.bfabric.service.SamplePreparationProtocolService;
import org.bfabric.service.UserService;
import org.bfabric.util.FileUploadHelper;
import org.bfabric.util.ImageCropperHelper;
import org.primefaces.PrimeFaces;

@MeasureCalls
@Named
@ViewScoped
public class InstrumentManager extends AbstractEntityManager<Instrument> {

    private static final long serialVersionUID = 1;

    @Inject
    SamplePreparationProtocolService samplePreparationProtocolService;

    @Inject
    FileUploadHelper fileUploadHelper;

    @Inject
    TechnologyList technologyList;

    @Inject
    ImageCropperHelper imageCropperHelper;

    private Annotation annotation;

    @Inject
    private AnnotationService annotationService;

    private boolean childContractsIncluded;

    @Inject
    private InstrumentService instrumentService;

    private String switchStatusComment;

    @Inject
    private UserService userService;

    public InstrumentManager() {
        super(Instrument.class);
    }

    public void clearServiceSelection() {
        getInstrument().setService(null);
        PrimeFaces.current().executeScript("PF('serviceSelection').unselectAllRows();");
    }

    public void createAnnotation() {
        setAnnotation(new Annotation());
        getAnnotation().setType(Instrument.class.getSimpleName().toLowerCase());
    }

    private String createFacesMessage(boolean isDeleted, String deletedEntityName, Map<String, List<Instrument>> updatedInstruments) {
        final StringBuilder msg = new StringBuilder(isDeleted ? Messages.get("successfullyDeleted") + " " + deletedEntityName : isCreated() ? Messages.get("successfullyCreated") : Messages
            .get("successfullyUpdated"));
        if (!updatedInstruments.get(Constants.DESCENDANT).isEmpty() || !updatedInstruments.get(Constants.ANCESTOR).isEmpty()) {
            msg.append(". Additionally updated");
            if (!updatedInstruments.get(Constants.DESCENDANT).isEmpty()) {
                msg.append(" descendants with id:");
                for (Instrument instrument : updatedInstruments.get(Constants.DESCENDANT)) {
                    msg.append(" ").append(instrument.getId()).append(",");
                }
                msg.deleteCharAt(msg.length() - 1);
            }

            if (!updatedInstruments.get(Constants.DESCENDANT).isEmpty() && !updatedInstruments.get(Constants.ANCESTOR).isEmpty()) {
                msg.append(" and ");
            }

            if (!updatedInstruments.get(Constants.ANCESTOR).isEmpty()) {
                msg.append(" ancestors with id:");
                for (Instrument instrument : updatedInstruments.get(Constants.ANCESTOR)) {
                    msg.append(" ").append(instrument.getId()).append(",");
                }
                msg.deleteCharAt(msg.length() - 1);
            }
        }
        return msg.toString();
    }

    @Override
    public Instrument createInstance() {
        Instrument instrument = super.createInstance();
        if (instrument != null) {
            List<Technology> technologies = technologyList.getTechnologiesEnabledIncludingTechnologies(instrument.getTechnologies());
            if (technologies != null && technologies.size() == 1) {
                instrument.addTechnology(technologies.get(0));
            }
        }
        return instrument;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public List<User> getBookers(String filterString) {
        return userService.getUsersFilteredExcluding(filterString, getInstrument().getBookers());
    }

    public List<SamplePreparationProtocol> getFilteredInstrumentSamplePreparationProtocols(String filterString) {
        return samplePreparationProtocolService.getFilteredInstrumentExcluding(filterString, getInstrument().getSamplePreparationProtocols());
    }

    @Produces
    @Named("instrument")
    public Instrument getInstrument() {
        return getInstance();
    }

    public List<User> getOperators(String filterString) {
        return userService.getUsersFilteredExcluding(filterString, getInstrument().getOperators());
    }

    public List<Instrument> getPotentialParentInstruments(String filterString) {
        Set<Instrument> exclude = null;
        if (getInstrument() != null && getInstrument().isManaged()) {
            exclude = new HashSet<>(getInstrument().getDescendants());
            exclude.add(getInstrument());
        }
        return instrumentService.getInstruments(filterString, null, exclude);
    }

    public List<?> getSelectionValuesByInstrumentFiltered(String filterString) {
        return annotationService.getAnnotationsByFilterAndType(filterString, Instrument.class.getSimpleName().toLowerCase());
    }

    public String getSwitchStatusComment() {
        return switchStatusComment;
    }

    public List<User> getTrainedUsers(String filterString) {
        return userService.getUsersFilteredExcluding(filterString, getInstrument().getTrainedUsers());
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getInstrument() != null) {
            getInstrument().setOldValues();
            imageCropperHelper.setImage(getInstrument().getImage());
        }
    }

    public boolean isChildContractsIncluded() {
        return childContractsIncluded;
    }

    public void purchasedPriceChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            getInstrument().setPurchasedPrice(new BigDecimal(event.getNewValue().toString()));
        } else {
            getInstrument().setPurchasedPrice(null);
        }
    }

    @Override
    public String remove() {
        String entityName = getInstrument().toString();
        Map<String, List<Instrument>> updatedInstruments = instrumentService.remove(getInstrument());

        // Create and print the faces message.
        getFacesMessagesManager().clearGlobalMessages();
        getFacesMessagesManager().printWarn(createFacesMessage(true, entityName, updatedInstruments));

        return getRedirectURLAfterRemove();
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = instrumentService.isValid(getInstrument());
        if (validationErrorMsg.isEmpty()) {
            setCreated(!isManaged());
            Map<String, List<Instrument>> updatedInstruments = instrumentService.save(getInstrument());

            // Create and print the faces message.
            getFacesMessagesManager().clearGlobalMessages();
            getFacesMessagesManager().bufferWarning(createFacesMessage(false, null, updatedInstruments));

            return postSave(false, false);
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
            getInstrument().setAnnotation(getAnnotation());
            setAnnotation(null);
        }
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public void setChildContractsIncluded(boolean childContractsIncluded) {
        this.childContractsIncluded = childContractsIncluded;
    }

    public void setSwitchStatusComment(String switchStatusComment) {
        this.switchStatusComment = switchStatusComment;
    }

    public String switchAvailable() {
        getInstance().switchAvailable(getSwitchStatusComment());
        save(true, true, false);
        getFacesMessagesManager().bufferWarningClear(Messages.get("instrumentStatus") + " : " + (getInstance().isAvailable() ? Messages.get("available") : Messages.get("notAvailable")));
        return getShowScreenRedirectURL();
    }

    public String switchBookable() {
        getInstance().switchBookable(getSwitchStatusComment());
        save(true, true, false);
        getFacesMessagesManager().bufferWarningClear(Messages.get("instrumentStatus") + " : " + (getInstance().isBookable() ? Messages.get("bookable") : Messages.get("notBookable")));
        return getShowScreenRedirectURL();
    }

    public String switchRunEnabled() {
        getInstance().switchRunEnabled(getSwitchStatusComment());
        save(true, true, false);
        getFacesMessagesManager().bufferWarningClear(Messages.get("instrumentStatus") + " : " + (getInstance().isRunEnabled() ? Messages.get("runEnabled") : Messages.get("notRunEnabled")));
        return getShowScreenRedirectURL();
    }

    public String switchUp() {
        getInstance().switchUp(getSwitchStatusComment());
        save(true, true, false);
        getFacesMessagesManager().bufferWarningClear(Messages.get("instrumentStatus") + " : " + (getInstance().isUp() ? Messages.get("up") : Messages.get("down")));
        return getShowScreenRedirectURL();
    }

    public String switchUserBookable() {
        getInstance().switchUserBookable(getSwitchStatusComment());
        save(true, true, false);
        getFacesMessagesManager().bufferWarningClear(Messages.get("instrumentStatus") + " : " + (getInstance().isUserBookable() ? Messages.get("userBookable") : Messages.get("notUserBookable")));
        return getShowScreenRedirectURL();
    }

    public String switchUserVisible() {
        getInstance().switchUserVisible(getSwitchStatusComment());
        save(true, true, false);
        getFacesMessagesManager().bufferWarningClear(Messages.get("instrumentStatus") + " : " + (getInstance().isUserVisible() ? Messages.get("userVisible") : Messages.get("notUserVisible")));
        return getShowScreenRedirectURL();
    }
}