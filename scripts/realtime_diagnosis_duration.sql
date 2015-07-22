#CREATE VIEW realtime_diagnosis
#==============================

DROP VIEW IF EXISTS `realtime_diagnosis`;
CREATE VIEW `realtime_diagnosis` AS
select `patients`.`pat_id` AS `pat_id`,
`patients`.`pat_sex` AS `pat_sex`,
`referral_records`.`rrc_clinic_date` AS `rrc_clinic_date`,
EXTRACT(YEAR FROM rrc_clinic_date) AS years,
((curdate() - interval 1 month) <= rrc_clinic_date) AS interval_1_month,
((curdate() - interval 3 month) <= rrc_clinic_date) AS interval_3_month,
((curdate() - interval 6 month) <= rrc_clinic_date) AS interval_6_month,
((curdate() - interval 9 month) <= rrc_clinic_date) AS interval_9_month,
`referral_records`.`rrc_est_id` AS `rrc_est_id`,
`referral_records`.`rrc_cli_id` AS `rrc_cli_id`,
`addresses`.`add_reporting_region` AS `add_reporting_region`,
`addresses`.`add_reporting_district` AS `add_reporting_district`,
`referral_records`.`rrc_text_answer` AS `rrc_text_answer` AS shortname,
`referral_records`.`rrc_answered_id` AS `rrc_answered_id`
from ((((`referral_records` join `referral_dir`) join `patients`) join `addresses`) join `clinics`)
where ((`referral_dir`.`red_rrc_id` = `referral_records`.`rrc_id`)
and (`referral_records`.`rrc_pat_id` = `patients`.`pat_id`)
and (`patients`.`pat_add_id` = `addresses`.`add_id`)
and (`referral_records`.`rrc_cli_id` = `clinics`.`cli_id`)
and (`clinics`.`cli_show` = 1)
and (`patients`.`pat_show` = 1)
and (`referral_dir`.`red_type` = 'D')
and ((not((`patients`.`pat_firstname` like '%test%'))) or (not((`patients`.`pat_surname` like '%test%'))))
and (`referral_dir`.`red_record_status` = 'approved')
and (`referral_records`.`rrc_category` = 'Not Shown')
and (`referral_records`.`rrc_answered_type` = 'Qs')
and (`referral_records`.`rrc_status` = 'approved'));
