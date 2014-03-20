<?php


include 'common.php';

include 'config.php';
 
$db = mysql_connect($hostname, $username, $password)
        or die('connect to database failed');
 
mysql_set_charset('utf8');
 
mysql_select_db($dbname)
        or die('db not found');

$query = 'DELETE FROM `stats`';

$result = mysql_query($query)
    or trigger_error(mysql_errno() . ' ' .
        mysql_error() . ' query: ' . $query);

    echo "{\"success\": true, ";
    echo "\"data\":{}}";

mysql_close($db);

?>

