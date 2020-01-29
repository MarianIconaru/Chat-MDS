//selecteaza prietenii anei
select f.* 
from people_friends
join people p
on people_id=p.id
join friends f
on friends_id=f.id
where p.porecla='ana';



select * 
from people_friends
join people p
on people_id=p.id
join friends f
on friends_id=f.id
where p.porecla='george' and f.porecla='andreea';

adaugare prieten pentru andrei(adaugare in tabelul people si actualizare in tabelul de legatura)
insert into people(porecla,parola)
values('andrei','andrei');
set @people_id=last_insert_id();
//in tabelul de legatura adaug un prieten existent(andreea) in baza de date catre o persoana(andrei)(MTM)
insert into people_friends(people_id,friends_id)
values((select id from people where porecla='andrei'),(select id from friends where porecla='andreea'));

insert into people(porecla,parola)
values('george','george');
insert into friends (porecla)
values ('gheorghe');
//daca las asa nu va merge selectul cu cele 2 joinuri 
//pentru ca nu am introdus in tabelul de legatura(nu sunt prieteni)


//adaugare 

insert into people(porecla,parola)
values ('ana','ana');
set @people_id=last_insert_id();

insert into friends(porecla)
values ('andreea');
set @friends_id=last_insert_id();

insert into friends(porecla)
values('mihai');
set @friends_id=last_insert_id();

//ana devine prieten cu mihai
insert into people_friends (people_id,friends_id)
values (@people_id,@friends_id);


create table people(
	id int not null auto_increment,
    porecla varchar(45) not null,
    parola varchar(45) not null,
    unique(porecla),
    primary key(id)
);
create table friends(
	id int not null auto_increment,
    porecla varchar(45) not null,
    unique(porecla),
    primary key(id)
);
create table people_friends(
	people_id int not null,
    friends_id int not null,
    primary key(people_id,friends_id),
    foreign key (people_id) references people(id),
    foreign key (friends_id) references friends(id)
);



pentru inserare prieteni
insert into friends