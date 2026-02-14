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

package org.bfabric;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JNLPParser {

    private static List<String> arguments;

    /**
     * Parse the JNLP file for run time configuration arguments.
     *
     * @param file The JNLP file
     * @return the list of JNLP arguments
     */
    public static List<String> parse(File file) {
        arguments = new ArrayList<>();
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            if (doc.hasChildNodes()) {
                traverse(doc.getChildNodes());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return arguments;
    }

    /**
     * Traverse the XML nodes recursively to find argument nodes.
     *
     * @param nodeList The nodeList for traversing
     */
    private static void traverse(NodeList nodeList) {
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node node = nodeList.item(count);
            // make sure it's element node.
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals("argument")) {
                    arguments.add(node.getTextContent());
                }
                if (node.hasChildNodes()) {
                    // loop again if there are no child nodes
                    traverse(node.getChildNodes());
                }
            }
        }
    }
}
