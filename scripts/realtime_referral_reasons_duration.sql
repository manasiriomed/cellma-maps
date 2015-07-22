#CREATE VIEW realtime_referral_reasons 
#========================================

DROP VIEW IF EXISTS `realtime_referral_reasons`;
CREATE VIEW `realtime_referral_reasons` AS 
select `patients`.`pat_id` AS `pat_id`,
`patients`.`pat_sex` AS `pat_sex`,
`referrals`.`ref_acceptance_date` AS `ref_acceptance_date`,
EXTRACT(YEAR FROM ref_acceptance_date) AS years,
((curdate() - interval 1 month) <= ref_acceptance_date) AS interval_1_month,
((curdate() - interval 3 month) <= ref_acceptance_date) AS interval_3_month,
((curdate() - interval 6 month) <= ref_acceptance_date) AS interval_6_month,
((curdate() - interval 9 month) <= ref_acceptance_date) AS interval_9_month,
`referral_reasons`.`rer_est_id` AS `rer_est_id`,
`referral_reasons`.`rer_cli_id` AS `rer_cli_id`,
`addresses`.`add_reporting_region` AS `add_reporting_region`,
`addresses`.`add_reporting_district` AS `add_reporting_district`,
`referral_reasons`.`rer_reason` AS shortname,
`referral_reasons`.`rer_eli_id` AS `rer_eli_id` 
from ((((`referral_reasons` join `referrals`) join `patients`) join `clinics`) join `addresses`) 
where ((`referral_reasons`.`rer_pat_id` = `patients`.`pat_id`) 
and (`patients`.`pat_add_id` = `addresses`.`add_id`) 
and (`referral_reasons`.`rer_cli_id` = `clinics`.`cli_id`) 
and (`referral_reasons`.`rer_ref_id` = `referrals`.`ref_id`) 
and (`patients`.`pat_show` = 1) 
and (`clinics`.`cli_show` = 1) 
and ((not((`patients`.`pat_firstname` like '%test%'))) or (not((`patients`.`pat_surname` like '%test%'))))
and (`referral_reasons`.`rer_reason_type` = 'referred'));


