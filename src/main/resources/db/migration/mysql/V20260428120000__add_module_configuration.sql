create table app_module
(
    id          char(36)     not null,
    module_code varchar(255) not null,
    enabled     boolean      not null default false,
    constraint pk_module primary key (id),
    constraint uk_module_code unique (module_code)
);

create table app_module_requirement
(
    module_id          char(36) not null,
    requires_module_id char(36) not null,
    constraint pk_module_requirement primary key (module_id, requires_module_id),
    constraint fk_module_requirement_module foreign key (module_id) references app_module (id),
    constraint fk_module_requirement_requirement foreign key (requires_module_id) references app_module (id)
);

insert into app_module (id, module_code, enabled)
values ('10c67221-1e9b-463d-b7e3-7a70f5172847', 'BANKING', true),
       ('6d86192c-6570-46ec-bfd1-b00c50ce7ab9', 'BUDGET', true),
       ('43b95efc-8e0d-4d3c-9466-3b62079839c3', 'CLASSIFICATION', true),
       ('e0d28a96-560a-44a2-a7f8-a49b22f04e0a', 'CONTRACT', true),
       ('d5f79448-0c97-475e-8e62-25738d1a9c1e', 'EXPORTER', true),
       ('e854748b-78fa-4476-b433-1a02f73971bf', 'SPENDING', true),
       ('94755e4c-7123-48b5-b915-045b1e218e61', 'SUGGESTION', true),
       ('2edaf4c4-41c5-4868-97a7-d5ca9f4913e7', 'INVOICE', false),
       ('aa295b78-2530-4d5a-b035-5c5dd5f47d34', 'PROJECT', false);

-- Add BANKING as a requirement for all modules
insert into app_module_requirement (module_id, requires_module_id)
select dependent.id,
       requirement.id
from app_module dependent,
     app_module requirement
where requirement.module_code = 'BANKING'
  and dependent.module_code in
      ('BUDGET', 'CLASSIFICATION', 'CONTRACT', 'EXPORTER', 'SPENDING', 'SUGGESTION', 'PROJECT', 'INVOICE');

insert into app_module_requirement (module_id, requires_module_id)
select dependent.id,
       requirement.id
from app_module dependent,
     app_module requirement
where requirement.module_code = 'BUDGET'
  and dependent.module_code in ('EXPORTER', 'SUGGESTION');

-- Add CLASSIFICATION as a requirement for all modules
insert into app_module_requirement (module_id, requires_module_id)
select dependent.id,
       requirement.id
from app_module dependent,
     app_module requirement
where requirement.module_code = 'CLASSIFICATION'
  and dependent.module_code in ('EXPORTER', 'SPENDING', 'SUGGESTION');

insert into app_module_requirement (module_id, requires_module_id)
select dependent.id,
       requirement.id
from app_module dependent,
     app_module requirement
where requirement.module_code = 'CONTRACT'
  and dependent.module_code in ('EXPORTER', 'SUGGESTION');

insert into app_module_requirement (module_id, requires_module_id)
select dependent.id,
       requirement.id
from app_module dependent,
     app_module requirement
where requirement.module_code = 'INVOICE'
  and dependent.module_code in ('PROJECT');
