-- V19: Ensure max_capacity >= actual enrolled student count per course
UPDATE courses c
SET max_capacity = sub.student_count
FROM (
    SELECT st.course_id, COUNT(*) AS student_count
    FROM student_twin st
    GROUP BY st.course_id
) sub
WHERE c.id = sub.course_id
  AND (c.max_capacity IS NULL OR c.max_capacity < sub.student_count);
