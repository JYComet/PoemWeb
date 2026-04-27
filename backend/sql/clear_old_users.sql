-- 清空旧用户数据（因密码加密方式改变，旧用户无法登录）
-- 执行后请重新注册用户

TRUNCATE TABLE poem_web.sys_user;

-- 重置自增ID
ALTER TABLE poem_web.sys_user AUTO_INCREMENT = 1;
