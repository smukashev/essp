<?php


include 'common.php';

include 'config.php';
 
$db = mysql_connect($hostname, $username, $password)
        or die('connect to database failed');
 
mysql_set_charset('utf8');
 
mysql_select_db($dbname)
        or die('db not found');

$query = 'SELECT count(*) as count FROM `stats`';
$result = mysql_query($query)
        or trigger_error(mysql_errno() . ' ' .
            mysql_error() . ' query: ' . $query);
$row = mysql_fetch_assoc($result);
echo "{\"total\":".$row['count'];

$limit_text = "";

if(array_key_exists('limit', $_POST))
{
	$limit_text .= " limit ".$_POST['limit'];

	if(array_key_exists('start', $_POST))
	{
		$limit_text .= " offset ".$_POST['start'];
	}
}

$sort_str = "";
if(array_key_exists('sort', $_POST))
{
    $sort = json_decode($_POST['sort']);

    $s_param = $sort[0];

    $sort_str .= " order by ".$s_param->property." ".$s_param->direction." ";
} else {
    $sort_str .= " order by `id` desc ";
}

$query = 'SELECT * FROM `stats` '.$sort_str.$limit_text;
$result = mysql_query($query)
        or trigger_error(mysql_errno() . ' ' .
            mysql_error() . ' query: ' . $query);
 
if (mysql_num_rows($result) > 0) {
	echo ",\"data\":[";
	$first = true;
	while ($row = mysql_fetch_assoc($result)) {
		if($first) {
			$first = false;
		} else {
			echo ",";
		}
        echo "{";
        echo "\"id\":".$row['id'].",";

        echo "\"r_queue_size\":".$row['r_queue_size'].",";
        echo "\"r_batches_in_progress\":".$row['r_batches_in_progress'].",";
        echo "\"r_batches_completed\":".$row['r_batches_completed'].",";

        echo "\"s_queue_size\":".$row['s_queue_size'].",";
        echo "\"s_threads_count\":".$row['s_threads_count'].",";
        echo "\"s_max_threads_count\":".$row['s_max_threads_count'].",";
        echo "\"s_avg_time\":".$row['s_avg_time'].",";

        echo "\"c_avg_processed\":".$row['c_avg_processed'].",";
        echo "\"c_avg_inserts\":".$row['c_avg_inserts'].",";
        echo "\"c_avg_selects\":".$row['c_avg_selects'].",";
        echo "\"c_total_processed\":".$row['c_total_processed'].",";

        echo "\"cr_date\":\"".preatifyDate($row['cr_date'])."\"";
        echo "}";
    }
    echo "]";
}
echo "}";
mysql_close($db);

?>