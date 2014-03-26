<?php

function preatifyDate($date)
{
	$datetime = strtotime($date);
	return date("d.m.Y H:i:s", $datetime);
}

function unPreatifyDateTime($date, $time)
{
	$datetime = strtotime($date.' '.$time);
	return date("Y-m-d H:i:s", $datetime);
}

function preatifyDateOnly($date)
{
	$datetime = strtotime($date);
	return date("d.m.Y", $datetime);
}

function preatifyTimeOnly($date)
{
	$datetime = strtotime($date);
	return date("H:i:s", $datetime);
}

function shortenName($last_name, $first_name, $middle_name)
{
	return $last_name.' '.mb_substr($first_name, 0, 1, 'UTF-8').'.'.mb_substr($middle_name, 0, 1, 'UTF-8').'.';
}


?>