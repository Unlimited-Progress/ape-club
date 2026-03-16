package com.jingdianjichi.circle.server.util;

import com.jingdianjichi.circle.api.common.TreeNode;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class TreeUtils {



//    buildTree 方法接收一个 List<T> 类型的参数 nodes，其中 T 是实现了 TreeNode 接口或继承了 TreeNode 抽象类的具体类型。该方法的目的是根据给定的节点列表构建一棵或多棵树，并返回这些树的根节点列表。
//
//    参数检查：首先检查输入的节点列表是否为空，如果为空，则直接返回空列表。
//    分组：使用 Java Stream API 对节点按父节点ID (nodePId) 进行分组，得到一个 Map<Long, List<TreeNode>>，其中键是父节点ID，值是具有相同父节点ID的所有节点组成的列表。
//    构建树：再次遍历节点列表，对于每个节点，从之前创建的分组映射中查找其子节点列表，并通过调用 pnd.setChildren(ts) 将找到的子节点列表设置到当前节点上。这里 peek 方法用于在流中的每个元素上执行副作用操作，即设置子节点。
//    过滤根节点：最后，使用 filter 方法筛选出所有根节点（即 getRootNode 返回 true 的节点），并将它们收集到一个新的列表中作为结果返回。

    public static <T extends TreeNode> List<T> buildTree(List<T> nodes) {

        if (CollectionUtils.isEmpty(nodes)) {
            return Collections.emptyList();
        }
        Map<Long, List<TreeNode>> groups = nodes.stream().collect(Collectors.groupingBy(TreeNode::getNodePId));
        return nodes.stream().filter(Objects::nonNull).peek(pnd -> {
            List<TreeNode> ts = groups.get(pnd.getNodeId());
            pnd.setChildren(ts);
        }).filter(TreeNode::getRootNode).collect(Collectors.toList());

    }

//    findAll 方法是一个递归方法，用于在树中查找指定ID的节点及其所有子节点，并将它们添加到结果列表中。
//
//    参数：
//    result：用于存储查找到的节点的结果列表。
//    node：当前正在检查的节点。
//    targetId：要查找的目标节点ID。
//    逻辑：
//    如果当前节点的ID或父节点ID与目标ID相匹配，那么将当前节点及其所有子节点添加到结果列表中。这是通过调用 addAll 辅助方法完成的。
//    如果当前节点没有子节点或者子节点中没有与目标ID匹配的节点，则递归地对每个子节点调用 findAll 方法继续搜索。
    public static <T extends TreeNode> void findAll(List<T> result, TreeNode node, Long targetId) {

        if (node.getNodeId().equals(targetId) || node.getNodePId().equals(targetId)) {
            addAll(result, node);
        } else {
            if (!CollectionUtils.isEmpty(node.getChildren())) {
                for (TreeNode child : node.getChildren()) {
                    findAll(result, child, targetId);
                }
            }
        }

    }


//    addAll 方法也是一个递归方法，用于将当前节点及其所有子节点添加到结果列表中。
//
//    参数：
//    result：结果列表。
//    node：当前要添加的节点。
//    逻辑：
//    首先将当前节点添加到结果列表中。
//    然后，如果当前节点有子节点，就对每个子节点递归调用 addAll 方法，直到所有子节点都被添加到结果列表中。

    private static <T extends TreeNode> void addAll(List<T> result, TreeNode node) {
        result.add((T) node);
        if (!CollectionUtils.isEmpty(node.getChildren())) {
            for (TreeNode child : node.getChildren()) {
                addAll(result, child);
            }
        }
    }

}
