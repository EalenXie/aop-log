DROP TABLE IF EXISTS `exception_response`;
CREATE TABLE `exception_response`  (
  `id` int(11) NOT NULL,
  `request_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `request_method` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `requester_ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `throwable` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `throwable_time` datetime NULL DEFAULT NULL,
  `request_body` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `user_agent` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `status_code` int(11) NULL DEFAULT NULL,
  `status_text` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status_reason_phrase` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `response_body` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `message` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `localized_message` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;