create table workerCampaign
(
    workerId   int not null,
    campaignId int not null,
    primary key (workerId, campaignId),
    constraint workerCampaign_campaign_fk
        foreign key (campaignId) references campaign (id)
            on update cascade on delete cascade,
    constraint workerCampaign_worker_fk
        foreign key (workerId) references user (id)
            on update cascade on delete cascade
);

