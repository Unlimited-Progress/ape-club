SET @login_id = 'oGXh93Hq0NTupmI_PnWRB60oaIKg';

UPDATE auth_permission
SET permission_key = 'subject:add'
WHERE permission_key = 'subject:add1';

SET @user_id = (
    SELECT id
    FROM auth_user
    WHERE user_name = @login_id
    LIMIT 1
);

INSERT INTO auth_role_permission (`role_id`, `permission_id`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`)
SELECT 1, 1, @login_id, NOW(), @login_id, NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM auth_role_permission
    WHERE role_id = 1
      AND permission_id = 1
      AND is_deleted = 0
);

INSERT INTO auth_user_role (`user_id`, `role_id`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`)
SELECT @user_id, 1, @login_id, NOW(), @login_id, NOW(), 0
FROM dual
WHERE @user_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM auth_user_role
      WHERE user_id = @user_id
        AND role_id = 1
        AND is_deleted = 0
  );
