<?php

include 'config.php';
 
$db = mysql_connect($hostname, $username, $password)
        or die('connect to database failed');
 
mysql_set_charset('utf8');
 
mysql_select_db($dbname)
        or die('db not found');

$query = 'CREATE TABLE IF NOT EXISTS `stats` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `r_queue_size` int(11) NOT NULL,
  `r_batches_in_progress` int(11) NOT NULL,
  `r_batches_completed` int(11) NOT NULL,
  `s_queue_size` int(11) NOT NULL,
  `s_threads_count` int(11) NOT NULL,
  `s_max_threads_count` int(11) NOT NULL,
  `s_avg_time` double NOT NULL,
  `c_avg_processed` double NOT NULL,
  `c_avg_inserts` double NOT NULL,
  `c_avg_selects` double NOT NULL,
  `c_total_processed` double NOT NULL,
  `cr_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
)';
$result = mysql_query($query)
        or trigger_error(mysql_errno() . ' ' .
            mysql_error() . ' query: ' . $query);

$query = 'CREATE  TABLE IF NOT EXISTS `commands` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `command` VARCHAR(1024) NOT NULL ,
  `sent` INT NOT NULL ,
  PRIMARY KEY (`id`) )';

$result = mysql_query($query)
        or trigger_error(mysql_errno() . ' ' .
            mysql_error() . ' query: ' . $query);

mysql_close($db);

?>