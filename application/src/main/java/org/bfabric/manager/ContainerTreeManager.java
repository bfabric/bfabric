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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Container;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.Resource;
import org.bfabric.entity.Sample;
import org.bfabric.entity.Workunit;
import org.bfabric.util.StringHelper;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

@Named
@ViewScoped
public class ContainerTreeManager implements Serializable {

    private static final long serialVersionUID = 1;

    private static final String DUMMY_NODE = "dummyNode";

    private TreeNode<Object> currentNode;

    private TreeNode<Object> rootNode = null;

    private static void addDummyNode(AbstractEntity entity, TreeNode<Object> node) {
        TreeNode<Object> dummyNode = new DefaultTreeNode<>(entity, node);
        dummyNode.setSelectable(true);
        dummyNode.getChildren().add(getDummyNode());
    }

    private static TreeNode<Object> getDummyNode() {
        return new DefaultTreeNode<>(DUMMY_NODE);
    }

    private static void loadDatasetTree(TreeNode<Object> node) {
        Dataset dataset = (Dataset) node.getData();
        loadNode(node, dataset.getSucceedingWorkunits(), Messages.get("succeedingWorkunits"));
    }

    private static void loadDatasetsTree(TreeNode<Object> node, Collection<Dataset> datasets) {
        for (Dataset dataset : datasets) {
            if (dataset.getSucceedingWorkunits().isEmpty()) {
                TreeNode<Object> leafNode = new DefaultTreeNode<>("leaf", dataset, node);
                leafNode.setSelectable(true);
            } else {
                addDummyNode(dataset, node);
            }
        }
    }

    private static void loadNode(TreeNode<Object> parentNode, Collection<?> collection, String label) {
        if (collection != null && !collection.isEmpty()) {
            String nodeLabel = StringHelper.isNotEmpty(label) ? label + " (" + collection.size() + ")" : Constants.EMPTY_STRING;
            TreeNode<Object> node = new DefaultTreeNode<>(nodeLabel, parentNode);
            node.setSelectable(false);
            node.getChildren().add(getDummyNode());
        }
    }

    private static void loadResourceTree(TreeNode<Object> node) {
        Resource resource = (Resource) node.getData();
        loadNode(node, resource.getSucceedingWorkunits(), Messages.get("succeedingWorkunits"));
        loadNode(node, resource.getSucceedingDatasets(), Messages.get("succeedingDatasets"));
    }

    private static void loadResourcesTree(TreeNode<Object> node, Collection<Resource> resources) {
        for (Resource resource : resources) {
            if (resource.getSucceedingWorkunits().isEmpty() && resource.getSucceedingDatasets().isEmpty()) {
                TreeNode<Object> leafNode = new DefaultTreeNode<>("leaf", resource, node);
                leafNode.setSelectable(true);
            } else {
                addDummyNode(resource, node);
            }
        }
    }

    private static void loadSampleTree(TreeNode<Object> node) {
        Sample sample = (Sample) node.getData();
        loadNode(node, sample.getChildren(), Messages.get("childSamples"));
        loadNode(node, sample.getWorkunits(), Messages.get("workunits"));
    }

    private static void loadSamplesTree(TreeNode<Object> node, Collection<Sample> samples) {
        for (Sample sample : samples) {
            if (sample.getWorkunits().isEmpty()) {
                TreeNode<Object> leafNode = new DefaultTreeNode<>("leaf", sample, node);
                leafNode.setSelectable(true);
            } else {
                addDummyNode(sample, node);
            }
        }
    }

    private static void loadWorkunitTree(TreeNode<Object> node) {
        Workunit workunit = (Workunit) node.getData();
        loadNode(node, workunit.getResources(), Messages.get("resources"));
        if (workunit.getDataset() != null) {
            TreeNode<Object> dataset = new DefaultTreeNode<>("leaf", workunit.getDataset(), node);
            dataset.setSelectable(true);
        }
    }

    private static void loadWorkunitsTree(TreeNode<Object> node, Collection<Workunit> workunits) {
        for (Workunit workunit : workunits) {
            if (workunit.getResources().isEmpty() && workunit.getDataset() == null) {
                TreeNode<Object> leafNode = new DefaultTreeNode<>("leaf", workunit, node);
                leafNode.setSelectable(true);
            } else {
                TreeNode<Object> resourcesNode = new DefaultTreeNode<>(workunit, node);
                resourcesNode.setSelectable(true);
                resourcesNode.getChildren().add(getDummyNode());
            }
        }
    }

    private static boolean nodeIsClass(TreeNode<Object> node, Class<?> clazz) {
        return clazz.isAssignableFrom(node.getData().getClass());
    }

    private static void removeDummyNode(TreeNode<Object> node) {
        List<TreeNode<Object>> children = node.getChildren();
        for (Iterator<TreeNode<Object>> iter = children.iterator(); iter.hasNext(); ) {
            TreeNode<Object> check = iter.next();
            if (check.getData().equals(DUMMY_NODE)) {
                iter.remove();
                break;
            }
        }
    }

    public TreeNode<Object> getCurrentNode() {
        return currentNode;
    }

    public TreeNode<Object> getTreeNode(AbstractEntity entity) {
        if (rootNode == null) {
            loadTree(entity);
        } else {
            if (!rootNode.getData().equals(entity)) {
                loadTree(entity);
            }
        }
        return rootNode;
    }

    private void loadTree(AbstractEntity entity) {
        rootNode = new DefaultTreeNode<>(entity, null);
        if (entity instanceof Container) {
            loadTreeContainer((Container) entity);
        } else if (entity instanceof Workunit) {
            loadWorkunitTree((Workunit) entity);
        } else if (entity instanceof Resource) {
            loadTreeResource((Resource) entity);
        } else if (entity instanceof Dataset) {
            loadTreeDataset((Dataset) entity);
        } else if (entity instanceof Sample) {
            loadTreeSample((Sample) entity);
        }
    }

    private void loadTreeContainer(Container container) {
        loadNode(rootNode, container.getSamples(), Messages.get("samples"));
        loadNode(rootNode, container.getWorkunits(), Messages.get("workunits"));
        loadNode(rootNode, container.getDatasets(), Messages.get("datasets"));
    }

    private void loadTreeDataset(Dataset dataset) {
        loadNode(rootNode, dataset.getSucceedingWorkunits(), Messages.get("succeedingWorkunits"));
        loadNode(rootNode, dataset.getAssociatedDatasets(), Messages.get("associatedDatasets"));
    }

    private void loadTreeResource(Resource resource) {
        loadNode(rootNode, resource.getSucceedingWorkunits(), Messages.get("succeedingWorkunits"));
        loadNode(rootNode, resource.getSucceedingDatasets(), Messages.get("succeedingDatasets"));
    }

    private void loadTreeSample(Sample sample) {
        loadNode(rootNode, sample.getChildren(), Messages.get("childSamples"));
        loadNode(rootNode, sample.getWorkunits(), Messages.get("workunits"));
        loadNode(rootNode, sample.getAssociatedDatasets(), Messages.get("associatedDatasets"));
    }

    private void loadWorkunitTree(Workunit workunit) {
        loadNode(rootNode, workunit.getResources(), Messages.get("resources"));
        loadNode(rootNode, workunit.getSucceedingWorkunits(), Messages.get("succeedingWorkunits"));
        loadNode(rootNode, workunit.getSucceedingDatasets(), Messages.get("succeedingDatasets"));
    }

    public void onNodeCollapse(NodeCollapseEvent event) {
        TreeNode<Object> parent = event.getTreeNode();
        List<TreeNode<Object>> children = parent.getChildren();
        Iterator<TreeNode<Object>> iter = children.iterator();
        while (iter.hasNext()) {
            TreeNode<Object> child = iter.next();
            child.clearParent();
            iter.remove();
        }
        // Add the dummyNode again.
        TreeNode<Object> dummy = getDummyNode();
        dummy.setParent(parent);
        parent.getChildren().add(dummy);
    }

    public void onNodeExpand(NodeExpandEvent event) {
        TreeNode<Object> expanded = event.getTreeNode();
        removeDummyNode(expanded);
        if (nodeIsClass(expanded, String.class)) {
            stringNodeExpanded(expanded);
        } else {
            if (nodeIsClass(expanded, Sample.class)) {
                loadSampleTree(expanded);
            } else if (nodeIsClass(expanded, Workunit.class)) {
                loadWorkunitTree(expanded);
            } else if (nodeIsClass(expanded, Resource.class)) {
                loadResourceTree(expanded);
            } else if (nodeIsClass(expanded, Dataset.class)) {
                loadDatasetTree(expanded);
            }
        }
    }

    public void setCurrentNode(TreeNode<Object> currentNode) {
        this.currentNode = currentNode;
    }

    private void stringNodeExpanded(TreeNode<Object> expanded) {
        // The 'Data' of the expanded folder is of class String --> nodeData = e.g., 'Name (42)'. Decision based on the 'Data' of the expanded node and of the class of its parent node.
        String nodeData = (String) expanded.getData();

        if (nodeData.startsWith(Messages.get("samples"))) {
            if (nodeIsClass(expanded.getParent(), Container.class)) {
                loadSamplesTree(expanded, ((Container) rootNode.getData()).getSamples());
            }
        } else if (nodeData.startsWith(Messages.get("childSamples"))) {
            if (nodeIsClass(expanded.getParent(), Sample.class)) {
                loadSamplesTree(expanded, ((Sample) expanded.getParent().getData()).getChildren());
            }
        } else if (nodeData.startsWith(Messages.get("workunits"))) {
            if (nodeIsClass(expanded.getParent(), Container.class)) {
                loadWorkunitsTree(expanded, ((Container) rootNode.getData()).getWorkunits());
            } else if (nodeIsClass(expanded.getParent(), Sample.class)) {
                loadWorkunitsTree(expanded, ((Sample) expanded.getParent().getData()).getWorkunits());
            }
        } else if (nodeData.startsWith(Messages.get("succeedingWorkunits"))) {
            if (nodeIsClass(expanded.getParent(), Dataset.class)) {
                loadWorkunitsTree(expanded, ((Dataset) expanded.getParent().getData()).getSucceedingWorkunits());
            } else if (nodeIsClass(expanded.getParent(), Workunit.class)) {
                loadWorkunitsTree(expanded, ((Workunit) expanded.getParent().getData()).getSucceedingWorkunits());
            } else if (nodeIsClass(expanded.getParent(), Resource.class)) {
                loadWorkunitsTree(expanded, ((Resource) expanded.getParent().getData()).getSucceedingWorkunits());
            }
        } else if (nodeData.startsWith(Messages.get("resources"))) {
            if (nodeIsClass(expanded.getParent(), Container.class)) {
                loadResourcesTree(expanded, ((Container) rootNode.getData()).getResources());
            } else if (nodeIsClass(expanded.getParent(), Workunit.class)) {
                loadResourcesTree(expanded, ((Workunit) expanded.getParent().getData()).getResources());
            }
        } else if (nodeData.startsWith(Messages.get("datasets")) && nodeIsClass(expanded.getParent(), Container.class)) {
            loadDatasetsTree(expanded, ((Container) rootNode.getData()).getDatasets());
        } else if (nodeData.startsWith(Messages.get("succeedingDatasets"))) {
            if (nodeIsClass(expanded.getParent(), Workunit.class)) {
                loadDatasetsTree(expanded, ((Workunit) expanded.getParent().getData()).getSucceedingDatasets());
            } else if (nodeIsClass(expanded.getParent(), Resource.class)) {
                loadDatasetsTree(expanded, ((Resource) expanded.getParent().getData()).getSucceedingDatasets());
            }
        }
        if (nodeData.startsWith(Messages.get("associatedDatasets"))) {
            if (nodeIsClass(expanded.getParent(), Workunit.class)) {
                loadDatasetsTree(expanded, ((Workunit) expanded.getParent().getData()).getAssociatedDatasets());
            } else if (nodeIsClass(expanded.getParent(), Resource.class)) {
                loadDatasetsTree(expanded, ((Resource) expanded.getParent().getData()).getAssociatedDatasets());
            } else if (nodeIsClass(expanded.getParent(), Sample.class)) {
                loadDatasetsTree(expanded, ((Sample) expanded.getParent().getData()).getAssociatedDatasets());
            }
        }
    }
}