<?php

include 'common.php';

include 'config.php';
 
$db = mysql_connect($hostname, $username, $password)
        or die('connect to database failed');
 
mysql_set_charset('utf8');
 
mysql_select_db($dbname)
        or die('db not found');

echo "{";

$query = 'INSERT INTO `commands` (`command`) VALUES (\''.$_POST['command'].'\')';

$result = mysql_query($query)
    or trigger_error(mysql_errno() . ' ' .
        mysql_error() . ' query: ' . $query);

$last_id = mysql_insert_id();
    
echo "\"success\": true";

echo "}";

mysql_close($db);

?>
