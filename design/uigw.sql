/*
Navicat MySQL Data Transfer

Source Server         : Host
Source Server Version : 50631
Source Host           : 192.168.1.11:3306
Source Database       : agms_zs

Target Server Type    : MYSQL
Target Server Version : 50631
File Encoding         : 65001

Date: 2016-07-07 10:44:27
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `t_uigw_interfaceinfo`
-- ----------------------------
DROP TABLE IF EXISTS `t_uigw_interfaceinfo`;
CREATE TABLE `t_uigw_interfaceinfo` (
  `id` varchar(32) NOT NULL,
  `interface_id` varchar(32) DEFAULT NULL COMMENT '接口ID',
  `interface_name` varchar(128) DEFAULT NULL COMMENT '接口名称',
  `invoke_url` varchar(255) DEFAULT NULL COMMENT '接口调用的URL',
  `invoke_method` varchar(64) DEFAULT NULL COMMENT '接口调用的方法',
  `system_id` varchar(32) DEFAULT NULL COMMENT '外部系统ID',
  `protocol_type` varchar(64) DEFAULT NULL COMMENT '协议类型',
  `request_type` varchar(32) DEFAULT NULL COMMENT '请求类型，http请求的POST、GET',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='外部系统接口信息表';

-- ----------------------------
-- Records of t_uigw_interfaceinfo
-- ----------------------------

-- ----------------------------
-- Table structure for `t_uigw_outerparams`
-- ----------------------------
DROP TABLE IF EXISTS `t_uigw_outerparams`;
CREATE TABLE `t_uigw_outerparams` (
  `id` varchar(32) NOT NULL,
  `system_id` varchar(32) DEFAULT NULL COMMENT '外部系统ID',
  `param_id` varchar(64) DEFAULT NULL COMMENT '参数字段ID，系统内部映射参数',
  `param_name` varchar(128) DEFAULT NULL COMMENT '参数名称',
  `param_desc` varchar(255) DEFAULT NULL COMMENT '参数描述',
  `default_value` varchar(64) NOT NULL COMMENT '默认值',
  `param_type` varchar(32) DEFAULT NULL COMMENT '参数类型： IN入参，OUT出参',
  `outer_param_id` varchar(64) DEFAULT NULL COMMENT '外部系统参数ID',
  `outer_param_name` varchar(128) DEFAULT NULL COMMENT '外部系统参数名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='接口参数配置表';

-- ----------------------------
-- Records of t_uigw_outerparams
-- ----------------------------
INSERT INTO `t_uigw_outerparams` VALUES ('1', 'DB', 'projectId', '项目ID', '项目唯一标识', '', 'string', 'projectId', '项目ID');
INSERT INTO `t_uigw_outerparams` VALUES ('2', 'DB', 'clientId', '客户ID', null, '', 'string', 'clientId', '客户ID');
INSERT INTO `t_uigw_outerparams` VALUES ('3', 'DB', 'classifyLevel', '质量分类结果', null, '', 'string', 'classifyLevel', '质量分类结果');
INSERT INTO `t_uigw_outerparams` VALUES ('4', 'DB', 'classifyDate', '质量分类时间', null, '', 'string', 'classifyDate', '质量分类时间');

-- ----------------------------
-- Table structure for `t_uigw_outersystem`
-- ----------------------------
DROP TABLE IF EXISTS `t_uigw_outersystem`;
CREATE TABLE `t_uigw_outersystem` (
  `id` varchar(32) NOT NULL,
  `system_id` varchar(32) DEFAULT NULL COMMENT '系统ID',
  `system_name` varchar(255) DEFAULT NULL COMMENT '系统名称',
  `system_desc` varchar(255) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='外部系统信息表';

-- ----------------------------
-- Records of t_uigw_outersystem
-- ----------------------------
INSERT INTO `t_uigw_outersystem` VALUES ('1', 'DBS', '担保系统', '与担保系统接口,更新描述');
INSERT INTO `t_uigw_outersystem` VALUES ('2', 'DBS', '担保系统', '与担保系统接口');

-- ----------------------------
-- Table structure for `t_uigw_sysinvokelog`
-- ----------------------------
DROP TABLE IF EXISTS `t_uigw_sysinvokelog`;
CREATE TABLE `t_uigw_sysinvokelog` (
  `id` varchar(32) NOT NULL COMMENT '记录ID',
  `system_id` varchar(32) DEFAULT NULL COMMENT '系统ID',
  `interface_id` varchar(128) DEFAULT NULL COMMENT '接口ID',
  `cost_time` varchar(32) DEFAULT NULL COMMENT '调用时长，毫秒',
  `log_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '调用时间',
  `invoke_type` varchar(10) DEFAULT NULL COMMENT '调用类型，1：本地调用外部系统，2：外部系统调用本系统',
  `local_addr` varchar(64) DEFAULT NULL COMMENT '请求的服务器IP地址',
  `remote_addr` varchar(64) DEFAULT NULL COMMENT '发起请求的客户端IP地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='统一接口调用日志信息表';

-- ----------------------------
-- Records of t_uigw_sysinvokelog
-- ----------------------------
