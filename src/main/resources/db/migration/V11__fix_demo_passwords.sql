-- Хеш из V09 не соответствует паролю «Demo1234!». Это правильный BCrypt(12) от «Demo1234!».
UPDATE users
SET    password_hash = '$2a$12$hfvmn6EMUMe208i6RsyG8u19Mik0euaAQw9Hpf.nk2FpiM9KXzwvS',
       updated_at    = NOW()
WHERE  is_demo = TRUE
  AND  password_hash = '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
