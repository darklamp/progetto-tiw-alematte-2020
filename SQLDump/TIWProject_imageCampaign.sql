create table imageCampaign
(
    campaignId int not null,
    imageId    int not null,
    primary key (campaignId, imageId),
    constraint imageCampaign_campaign_fk
        foreign key (campaignId) references campaign (id)
            on update cascade on delete cascade,
    constraint imageCampaign_image_fk
        foreign key (imageId) references image (id)
            on update cascade on delete cascade
);

