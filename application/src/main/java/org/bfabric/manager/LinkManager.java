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

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.bfabric.entity.Link;
import org.bfabric.interceptors.MeasureCalls;

@MeasureCalls
@Named
@ViewScoped
public class LinkManager extends AbstractEntityManager<Link> {

    private static final long serialVersionUID = 1;

    public LinkManager() {
        super(Link.class);
    }

    public String checkValidity() {
        getInstance().checkValidity();
        return save();
    }

    @Produces
    @Named("link")
    public Link getLink() {
        return getInstance();
    }
}
