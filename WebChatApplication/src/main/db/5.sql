select count(name) name,user_id from users 
join messages on messages.user_id = users.name 
group by users.name
having count(name) > 3