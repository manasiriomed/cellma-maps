#CREATE VIEW realtime_appointment_reasons 
#========================================

DROP VIEW IF EXISTS `realtime_appointment_reasons`;
CREATE VIEW `realtime_appointment_reasons` AS 
select `patients`.`pat_id` AS `pat_id`,
`patients`.`pat_sex` AS `pat_sex`,
`r`.`rea_date` AS `rea_date`,
EXTRACT(YEAR FROM rea_date) AS years,
((curdate() - interval 1 month) <= rea_date) AS interval_1_month,
((curdate() - interval 3 month) <= rea_date) AS interval_3_month,
((curdate() - interval 6 month) <= rea_date) AS interval_6_month,
((curdate() - interval 9 month) <= rea_date) AS interval_9_month,
(select `establishment_list_items`.`eli_id` 
from (`establishment_list_items` join `application_lists`) 
where (`establishment_list_items`.`eli_app_id` = `application_lists`.`app_id`) 
and (`application_lists`.`app_name` = 'Appointment Reason')
and (`establishment_list_items`.`eli_text` = `r`.`rea_review_reason`)) AS `rea_review_reason_eli_id`,
`r`.`rea_review_reason` AS `rea_review_reason` AS shortname,
`r`.`rea_est_id` AS `rea_est_id`,
`r`.`rea_cli_id` AS `rea_cli_id`,
`addresses`.`add_address6` AS `add_address6`,
`addresses`.`add_reporting_region` AS `add_reporting_region`,
`addresses`.`add_reporting_district` AS `add_reporting_district` 
from (((`referral_appointments` `r` join `patients`) join `addresses`) join `clinics`) 
where ((`r`.`rea_record_status` = 'approved') 
and (`r`.`rea_status` <> 'cancelled') 
and (`r`.`rea_pat_id` = `patients`.`pat_id`) 
and (`patients`.`pat_add_id` = `addresses`.`add_id`) 
and (`r`.`rea_cli_id` = `clinics`.`cli_id`) 
and (`r`.`rea_review_reason` is not null) 
and (`clinics`.`cli_show` = 1) 
and (`patients`.`pat_show` = 1) 
and ((not((`patients`.`pat_firstname` like '%test%'))) or (not((`patients`.`pat_surname` like '%test%')))));

