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

package org.bfabric.service;

import java.util.LinkedHashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.FeedbackQuestion;
import org.bfabric.entity.FeedbackQuestionCustomOption;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class FeedbackQuestionService extends AbstractService {

    private static final long serialVersionUID = 1;

    public FeedbackQuestionService() {
        super(FeedbackQuestion.class);
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final FeedbackQuestion feedbackQuestion = (FeedbackQuestion) entity;
        LinkedHashMap<String, String> errorMsg = new LinkedHashMap<>();

        if (feedbackQuestion.getFeedbackQuestionType().isRequiresCustomOptions()) {
            final List<FeedbackQuestionCustomOption> customOptions = feedbackQuestion.getCustomOptions();
            if (customOptions.isEmpty()) {
                errorMsg.put(Constants.EDIT + ":customOptions", Messages.get("valueMustBeSet"));
            }
            for (int i = 0; i < customOptions.size(); i++) {
                if (StringHelper.isEmpty(customOptions.get(i).getValue())) {
                    errorMsg.put(Constants.EDIT + ":customOption:" + i + ":optionValue", Messages.get("valueMustBeSet"));
                } else {
                    for (int j = i + 1; j < customOptions.size(); j++) {
                        if (StringHelper.isNotEmpty(customOptions.get(j).getValue()) && customOptions.get(i).getValue().equals(customOptions.get(j).getValue())) {
                            errorMsg.put(Constants.EDIT + ":customOption:" + i + ":optionValue", Messages.get("valueNotUnique"));
                            errorMsg.put(Constants.EDIT + ":customOption:" + j + ":optionValue", Messages.get("valueNotUnique"));
                        }
                    }
                }
            }
        }

        return errorMsg;
    }
}