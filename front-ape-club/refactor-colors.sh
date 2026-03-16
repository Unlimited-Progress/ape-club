#!/bin/bash

# 批量重构脚本 - 将硬编码颜色替换为变量

# 定义颜色映射
declare -A color_map=(
  ["#3c6eee"]="@primary-color"
  ["rgba(60, 110, 238, 0.9)"]="@primary-hover"
  ["rgba(60, 110, 238, 0.1)"]="@primary-light"
  ["rgba(60, 110, 238, 0.5)"]="@grade-1"
  ["rgba(60, 110, 238, 0.6)"]="@grade-2"
  ["rgba(60, 110, 238, 0.7)"]="@grade-3"
  ["rgba(60, 110, 238, 0.8)"]="@grade-4"
  ["#1890ff"]="@info-color"
  ["#666666"]="@text-tertiary"
  ["#999999"]="@text-quaternary"
  ["#000000"]="@text-primary"
  ["#333333"]="@text-secondary"
  ["#ffffff"]="@bg-white"
  ["#fff"]="@bg-white"
  ["#f3f4f6"]="@bg-gray-2"
  ["#f2f3f5"]="@bg-gray-2"
  ["#eee"]="@bg-gray-4"
  ["#d9d9d9"]="@border-base"
  ["#e0e0e0"]="@border-light"
)

echo "颜色映射表已创建"
