<?php

$postdata = file_get_contents("php://input");

$stats = json_decode($postdata);

include 'config.php';
 
$db = mysql_connect($hostname, $username, $password)
        or die('connect to database failed');
 
mysql_set_charset('utf8');
 
mysql_select_db($dbname)
        or die('db not found');

if ($stats != null) {
	$query = "INSERT INTO `usci`.`stats`
				(
				`r_queue_size`,
				`r_batches_in_progress`,
				`r_batches_completed`,
				`s_queue_size`,
				`s_threads_count`,
				`s_max_threads_count`,
				`s_avg_time`,
				`c_avg_processed`,
				`c_avg_inserts`,
				`c_avg_selects`,
				`c_total_processed`)
				VALUES
				(
				".$stats->receiverStatus->queueSize.",
				".$stats->receiverStatus->batchesInProgress.",
				".$stats->receiverStatus->batchesCompleted.",

				".$stats->syncStatus->queueSize.",
				".$stats->syncStatus->threadsCount.",
				".$stats->syncStatus->maxThreadsCount.",
				".$stats->syncStatus->avgTime.",

				".$stats->coreStatus->avgProcessed.",
				".$stats->coreStatus->avgInserts.",
				".$stats->coreStatus->avgSelects.",
				".$stats->coreStatus->totalProcessed."
				);";
	$result = mysql_query($query)
	        or trigger_error(mysql_errno() . ' ' .
	            mysql_error() . ' query: ' . $query);

	$query = "SELECT
		`commands`.`id`,
		`commands`.`command`,
		`commands`.`sent`
		FROM `commands` WHERE sent = 0 limit 1";

	$result = mysql_query($query)
	        or trigger_error(mysql_errno() . ' ' .
	            mysql_error() . ' query: ' . $query);

	if (mysql_num_rows($result) > 0) {
		$row = mysql_fetch_assoc($result);

		$id = $row['id'];
		$command = $row['command'];

		$query = "UPDATE `commands`
			SET
			`sent` = 1
			WHERE id = ".$id;

		$result = mysql_query($query)
		        or trigger_error(mysql_errno() . ' ' .
		            mysql_error() . ' query: ' . $query);

		echo "COMMAND: ".$command;

	} else echo "COMMAND: nocommand";
} else {
	echo "ERROR: Can't decode JSON data";
}

mysql_close($db);

?>