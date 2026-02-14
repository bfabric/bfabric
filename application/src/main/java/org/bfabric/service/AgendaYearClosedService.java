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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AgendaYearClosed;
import org.bfabric.entity.Credit;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Role;
import org.bfabric.entity.User;
import org.bfabric.enums.CreditTypeEnum;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.IndexHelper;

@Named
@Stateless
public class AgendaYearClosedService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(AgendaYearClosedService.class.getName());

    @Inject
    private EntityService entityService;

    public AgendaYearClosedService() {
        super(AgendaYearClosed.class);
    }

    public void closeAgendaForPreviousYear() {
        int agendaYear = LocalDateTime.now().getYear() - 1;
        try {
            if (isOpen(agendaYear)) {
                closeAgendaYear(agendaYear, (User) entityService.createNamedQuery("User.findByLogin").setParameter("login", "admin").getSingleResult());
                logger.info("Agenda year " + agendaYear + " successfully closed");
            } else {
                logger.info("Agenda year " + agendaYear + " is already closed");
            }
        } catch (NoResultException e) {
            logger.info("Agenda year not closable since user admin does not exist!");
        }
    }

    public List<String> closeAgendaYear(int agendaYear, User currentUser) {

        // Collect all new credits for later indexing.
        final List<Credit> indexCredits = new ArrayList<>();

        final List<String> employees = new ArrayList<>();
        int userCount = 0;
        BigDecimal creditsNew;
        BigDecimal creditsHave;

        final List<User> users = createNamedQuery("User.findEmployees").getResultList();
        for (final User user : users) {
            if (user != null) {
                userCount++;

                creditsNew = user.getCreditsYearly(agendaYear + 1);

                final Credit creditNew = new Credit();
                creditNew.setUser(user);
                creditNew.setYear(agendaYear + 1);
                creditNew.setType(CreditTypeEnum.VACATION);
                creditNew.setDescription(Messages.get("vacationCreditFor") + " " + (agendaYear + 1));
                creditNew.setDays(creditsNew);

                // Save and put into list for later indexing.
                persist(creditNew);
                indexCredits.add(creditNew);

                creditsHave = user.getCreditsTotalByYear(agendaYear).subtract(user.getAccountedDaysByYear(agendaYear));
                final Credit creditHave = new Credit();
                creditHave.setDays(creditsHave);

                // If there are positive (remaining) or negative (overdrawn) credits, then create a corresponding credit entity for the new year.
                if (creditsHave.doubleValue() != 0) {
                    creditHave.setUser(user);
                    creditHave.setYear(agendaYear + 1);
                    if (creditsHave.doubleValue() > 0) {
                        creditHave.setType(CreditTypeEnum.REMAINING);
                        creditHave.setDescription(Messages.get("remainingCreditsFrom") + " " + agendaYear);
                    } else {
                        creditHave.setType(CreditTypeEnum.OVERDRAWN);
                        creditHave.setDescription(Messages.get("overdrawnCreditsFrom") + " " + agendaYear);
                    }
                    // Save and put into list for later indexing.
                    persist(creditHave);
                    indexCredits.add(creditHave);
                }

                employees
                    .add(user.getLastName() + ": " + creditNew.getDays() + (creditHave.getDays().doubleValue() < 0 ? " - " + -1 * creditHave.getDays().doubleValue() : " + " + creditHave.getDays()));
            }
        }

        // Index all newly given credits.
        IndexHelper.indexEntities(indexCredits);

        final AgendaYearClosed agendaYearClosed = new AgendaYearClosed();
        agendaYearClosed.setYear(agendaYear);
        persist(agendaYearClosed);

        // Build report text.
        List<String> closeAgendaYearResult = new ArrayList<>();
        closeAgendaYearResult.add(userCount + " " + Messages.get("employeesProcessed"));
        closeAgendaYearResult.addAll(employees);

        final Mail mail = new Mail();
        mail.setParent(currentUser);
        mail.setType(MailTypeEnum.AGENDA_CLOSE_YEAR, Constants.EMPTY_STRING, Long.valueOf(agendaYear).toString());
        mail.setRecipient(currentUser);
        Set<User> recipients = new HashSet<>();
        Role role = getRoleByRoleEnum(RoleEnum.AGENDAMANAGER);
        if (role != null) {
            recipients = role.getUsers();
        }
        mail.addRecipients(recipients);
        mail.setInput("closeAgendaYear", agendaYear);
        mail.setInput("nextAgendaYear", agendaYear + 1);
        mail.setInput("closeAgendaYearResult", closeAgendaYearResult);
        mailSendService.send(mail);

        return closeAgendaYearResult;
    }

    public boolean isOpen() {
        return isOpen(LocalDate.now().getYear() - 1);
    }

    public boolean isOpen(int agendaYear) {
        return createNamedQuery("AgendaYearClosed.existsByYear").setParameter("year", agendaYear).setMaxResults(1).getResultList().isEmpty();
    }
}