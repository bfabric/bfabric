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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.bfabric.entity.Sample;
import org.bfabric.interceptors.CachedMethodResult;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

@Named
@ViewScoped
public class SampleTreeTableManager extends AbstractManager {

    private static final long serialVersionUID = 1;

    private static final String rootNodeSampleName = "::__ROOT_NODE_SAMPLE_NAME__::";

    private static final String defaultChildNodeSampleName = "::__DEFAULT_CHILD_NODE_SAMPLE_NAME__::";

    private Sample defaultChildNodeSample;

    private boolean lazySampleTree;

    private Sample parentSample;

    private TreeNode<Sample> rootNode;

    private Sample rootNodeSample;

    private Set<Sample> samples;

    private boolean treeTableView = false;

    public void addChildSamplesToNode(Sample aSample, DefaultTreeNode<Sample> parent, Set<Sample> addedSamples) {
        for (Sample sample : aSample.getChildren()) {
            if (!sample.isChecked()) {
                sample.setChecked(true);
                addedSamples.add(sample);
                addChildSamplesToNode(sample, new DefaultTreeNode<>(sample, parent), addedSamples);
            }
        }
    }

    public void buildSampleTree() {
        setRootNode(new DefaultTreeNode<>(getRootNodeSample(), null));

        Set<Sample> addedSamples = new HashSet<>();
        Set<Sample> topLevelNodesWithoutChildren = new HashSet<>();
        for (Sample sample : getSamples()) {
            if (!sample.getChildren().isEmpty() && (sample.getParents().isEmpty() || sample.getParents().contains(getParentSample()))) {
                sample.setChecked(true);
                if (isLazySampleTree()) {
                    DefaultTreeNode<Sample> node = new DefaultTreeNode<>(sample, getRootNode());
                    new DefaultTreeNode<>(getDefaultChildNodeSample(), node);
                    addedSamples.add(sample);
                } else {
                    addChildSamplesToNode(sample, new DefaultTreeNode<>(sample, getRootNode()), addedSamples);
                }
            } else {
                topLevelNodesWithoutChildren.add(sample);
            }
        }

        topLevelNodesWithoutChildren.removeAll(addedSamples);
        for (Sample sample : topLevelNodesWithoutChildren) {
            if (isLazySampleTree()) {
                if (sample.getParents().isEmpty()) {
                    new DefaultTreeNode<>(sample, getRootNode());
                }
            } else {
                new DefaultTreeNode<>(sample, getRootNode());
            }
        }
    }

    public Sample getDefaultChildNodeSample() {
        if (defaultChildNodeSample == null) {
            defaultChildNodeSample = new Sample();
            defaultChildNodeSample.setName(defaultChildNodeSampleName);
            return defaultChildNodeSample;
        }
        return defaultChildNodeSample;
    }

    public Sample getParentSample() {
        return parentSample;
    }

    public TreeNode<Sample> getRootNode() {
        return rootNode;
    }

    public Sample getRootNodeSample() {
        if (rootNodeSample == null) {
            rootNodeSample = new Sample();
            rootNodeSample.setName(rootNodeSampleName);
        }
        return rootNodeSample;
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    @CachedMethodResult
    public TreeNode<Sample> getTreeTableContent(Set<Sample> aSamples, Sample aParentSample, boolean aLazySampleTree) {
        if (getRootNode() == null) {
            setSamples(aSamples);
            setParentSample(aParentSample);
            setLazySampleTree(aLazySampleTree);
            buildSampleTree();
        }
        return getRootNode();
    }

    public boolean isDefaultChildNodeSample(Sample sample) {
        return sample.getName().equals(defaultChildNodeSampleName) && sample.getId() == 0;
    }

    public boolean isLazySampleTree() {
        return lazySampleTree;
    }

    public boolean isTreeTableView() {
        return treeTableView;
    }

    public void nodeCollapseListener(NodeCollapseEvent event) {
        TreeNode<Sample> collapsedNode = (TreeNode<Sample>) event.getTreeNode();
        if (isLazySampleTree()) {
            collapsedNode.getChildren().clear();
            new DefaultTreeNode<>(getDefaultChildNodeSample(), collapsedNode);
        }
    }

    public void nodeExpandListener(NodeExpandEvent event) {
        TreeNode<Sample> expandedNode = (TreeNode<Sample>) event.getTreeNode();
        Sample expandedSample = expandedNode.getData();
        if (isLazySampleTree()) {
            List<TreeNode<Sample>> childrenNodes = expandedNode.getChildren();
            if (childrenNodes.size() == 1 && isDefaultChildNodeSample(childrenNodes.get(0).getData())) {
                expandedNode.getChildren().clear();
            }

            for (Sample childSample : expandedSample.getChildren()) {
                DefaultTreeNode<Sample> node = new DefaultTreeNode<>(childSample, expandedNode);
                if (!childSample.getChildren().isEmpty()) {
                    new DefaultTreeNode<>(getDefaultChildNodeSample(), node);
                }
            }
        }
    }

    @SuppressWarnings("EmptyMethod")
    public void pageListener() {
    }

    public void setLazySampleTree(boolean lazySampleTree) {
        this.lazySampleTree = lazySampleTree;
    }

    public void setParentSample(Sample parentSample) {
        this.parentSample = parentSample;
    }

    public void setRootNode(TreeNode<Sample> rootNode) {
        this.rootNode = rootNode;
    }

    public void setSamples(Set<Sample> samples) {
        this.samples = samples;
    }

    public void setTreeTableView(boolean treeTableView) {
        this.treeTableView = treeTableView;
    }

    public void switchTreeTableView() {
        setTreeTableView(!this.treeTableView);
    }

}