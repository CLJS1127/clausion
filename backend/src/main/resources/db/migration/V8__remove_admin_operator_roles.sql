-- Remove admin and operator users
DELETE FROM users WHERE role IN ('ADMIN', 'OPERATOR');
