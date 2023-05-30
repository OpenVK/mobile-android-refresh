package uk.openvk.android.refresh.api.enumerations;

public class HandlerMessages {
    // Authorization
    public static int OVKAPI_AUTHORIZED                        =   1;
    public static int OVKAPI_INVALID_USERNAME_OR_PASSWORD      =   2;
    public static int OVKAPI_TWOFACTOR_CODE_REQUIRED           =   3;

    // Account
    public static int OVKAPI_ACCOUNT_PROFILE_INFO              = 100;
    public static int OVKAPI_ACCOUNT_INFO                      = 101;
    public static int OVKAPI_ACCOUNT_SET_TO_ONLINE             = 102;
    public static int OVKAPI_ACCOUNT_SET_TO_OFFLINE            = 103;
    public static int OVKAPI_ACCOUNT_COUNTERS                  = 104;

    // Friends
    public static int OVKAPI_FRIENDS_GET                       = 200;
    public static int OVKAPI_FRIENDS_GET_MORE                  = 201;
    public static int OVKAPI_FRIENDS_GET_ALT                   = 202;
    public static int OVKAPI_FRIENDS_ADD                       = 203;
    public static int OVKAPI_FRIENDS_DELETE                    = 204;
    public static int OVKAPI_FRIENDS_CHECK                     = 205;
    public static int OVKAPI_FRIENDS_REQUESTS                  = 206;

    // Groups
    public static int OVKAPI_GROUPS_GET                        = 300;
    public static int OVKAPI_GROUPS_GET_MORE                   = 301;
    public static int OVKAPI_GROUPS_GET_ALT                    = 302;
    public static int OVKAPI_GROUPS_GET_BY_ID                  = 303;
    public static int OVKAPI_GROUPS_SEARCH                     = 304;
    public static int OVKAPI_GROUPS_JOIN                       = 305;
    public static int OVKAPI_GROUPS_LEAVE                      = 306;

    // Likes
    public static int OVKAPI_LIKES_ADD                         = 400;
    public static int OVKAPI_LIKES_DELETE                      = 401;
    public static int OVKAPI_LIKES_CHECK                       = 402;

    // Messages
    public static int OVKAPI_MESSAGES_GET_BY_ID                = 500;
    public static int OVKAPI_MESSAGES_SEND                     = 501;
    public static int OVKAPI_MESSAGES_DELETE                   = 502;
    public static int OVKAPI_MESSAGES_RESTORE                  = 503;
    public static int OVKAPI_MESSAGES_CONVERSATIONS            = 504;
    public static int OVKAPI_MESSAGES_GET_CONVERSATIONS_BY_ID  = 505;
    public static int OVKAPI_MESSAGES_GET_HISTORY              = 506;
    public static int OVKAPI_MESSAGES_GET_LONGPOLL_HISTORY     = 507;
    public static int OVKAPI_MESSAGES_GET_LONGPOLL_SERVER      = 508;

    // Users
    public static int OVKAPI_USERS_GET                         = 600;
    public static int OVKAPI_USERS_GET_ALT                     = 601;
    public static int OVKAPI_USERS_GET_ALT2                    = 602;
    public static int OVKAPI_USERS_FOLLOWERS                   = 603;
    public static int OVKAPI_USERS_SEARCH                      = 604;

    // Wall
    public static int OVKAPI_WALL_GET                          = 700;
    public static int OVKAPI_WALL_GET_BY_ID                    = 701;
    public static int OVKAPI_WALL_POST                         = 702;
    public static int OVKAPI_WALL_REPOST                       = 703;
    public static int OVKAPI_WALL_CREATE_COMMENT               = 704;
    public static int OVKAPI_WALL_DELETE_COMMENT               = 705;
    public static int OVKAPI_WALL_COMMENT                      = 706;
    public static int OVKAPI_WALL_ALL_COMMENTS                 = 707;

    // Newsfeed
    public static int OVKAPI_NEWSFEED_GET                      = 800;
    public static int OVKAPI_NEWSFEED_GET_GLOBAL               = 801;
    public static int OVKAPI_NEWSFEED_GET_MORE                 = 802;
    public static int OVKAPI_NEWSFEED_GET_MORE_GLOBAL          = 803;

    // OpenVK specific
    public static int OVKAPI_OVK_VERSION                       = 900;
    public static int OVKAPI_OVK_TEST                          = 901;
    public static int OVKAPI_OVK_CHICKEN_WINGS                 = 902;
    public static int OVKAPI_OVK_ABOUTINSTANCE                 = 903;
    public static int OVKAPI_OVK_CHECK_HTTP                    = 904;
    public static int OVKAPI_OVK_CHECK_HTTPS                   = 905;

    // PollAttachment
    public static int OVKAPI_POLL_ADD_VOTE                     = 1000;
    public static int OVKAPI_POLL_DELETE_VOTE                  = 1001;

    // Misc
    public static int DLM_ACCOUNT_AVATAR                       = 1100;
    public static int DLM_NEWSFEED_ATTACHMENTS                 = 1101;
    public static int DLM_WALL_ATTACHMENTS                     = 1102;
    public static int DLM_WALL_AVATARS                         = 1103;
    public static int DLM_NEWSFEED_AVATARS                     = 1104;
    public static int DLM_PROFILE_AVATARS                      = 1105;
    public static int DLM_GROUP_AVATARS                        = 1106;
    public static int DLM_GROUP_AVATARS_ALT                    = 1107;
    public static int DLM_FRIEND_AVATARS                       = 1108;
    public static int DLM_COMMENT_AVATARS                      = 1109;
    public static int DLM_CONVERSATIONS_AVATARS                = 1110;
    public static int LONGPOLL                                 = 1111;
    public static int DLM_ORIGINAL_PHOTO                       = 1112;

    // Errors
    public static int OVKAPI_NO_INTERNET_CONNECTION            =  -1;
    public static int OVKAPI_CONNECTION_TIMEOUT                =  -2;
    public static int OVKAPI_INVALID_JSON_RESPONSE             =  -3;
    public static int OVKAPI_INVALID_USAGE                     =  -4;
    public static int OVKAPI_INVALID_TOKEN                     =  -5;
    public static int OVKAPI_CHAT_DISABLED                     =  -6;
    public static int OVKAPI_METHOD_NOT_FOUND                  =  -7;
    public static int OVKAPI_ACCESS_DENIED                     =  -8;
    public static int OVKAPI_ACCESS_DENIED_MARSHMALLOW         =  -9;
    public static int OVKAPI_BROKEN_SSL_CONNECTION             =  -10;
    public static int OVKAPI_INTERNAL_ERROR                    =  -11;
    public static int OVKAPI_INSTANCE_UNAVAILABLE              =  -12;
    public static int OVKAPI_NOT_OPENVK_INSTANCE               =  -13;
    public static int OVKAPI_UNKNOWN_ERROR                     =  -14;

    public static int DLM_NO_INTERNET_CONNECTION               =  -101;
    public static int DLM_CONNECTION_TIMEOUT                   =  -2;
    public static int DLM_INVALID_USAGE                        =  -4;
    public static int DLM_ACCESS_DENIED                        =  -8;
    public static int DLM_BROKEN_SSL_CONNECTION                =  -10;
    public static int DLM_INTERNAL_ERROR                       =  -11;
    public static int DLM_INSTANCE_UNAVAILABLE                 =  -12;
    public static int DLM_NOT_OPENVK_INSTANCE                  =  -13;
    public static int DLM_UNKNOWN_ERROR                        =  -14;
}
