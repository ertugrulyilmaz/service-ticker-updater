CREATE TABLE `coin` (
  `symbol` varchar(10) NOT NULL,
  `name` varchar(100) NOT NULL,
  `category` varchar(30) DEFAULT'',
  `price` double(19,8) NOT NULL,
  `order_no` int(11) NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`symbol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
