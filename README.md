
# Domain model objects

## User

```json
{
    "name": @username,
    "first_tags": [<tag_obj>,...], //first 5 tags
    "tags": "/users/@username/tags", //hyperlink to all user tags
    "network": "facebook|twitter"
    /* last_preview: <timestamp>,
    //hyperlink to user updates since last_preview except updates>previewed=true
    updates: "/users/@username/updates"
    updates_count: "/users/@username/updates/count"*/
}
```
## Tag
```json
{
    last_preview: <timestamp>,
    name: @tag_name,
    first_users: [<user_obj>, ...],
    users: "/tags/@tagname/users", //hyperlink to all users of this tag
    //hyperlink to tag updates since last_preview except updates>previewed=true
    updates: "/tags/@tagname/updates"
    updates_count: "/tags/@tagname/updates/count"
}

```
## list
```json
{
    "name": "smart list #1",
    "first_tags": [<tag_obg>, ...],
    "tags": "hyperlink to tags",
    first_users: [<user_obj>, ...],
    users: "/lists/@listname/users", //hyperlink to all users of this tag
    "updates": "hyperlink to updates",
    "updates_count": "hyperlink to updates count",
    "last_preview": 12345678910
}
```


## Update

```json
{
    user: @username,
    timestamp: <ts>,
    previewed: false|true,
    text: <string>
}
```


# Operations
## Preview updates
show all updates with preview == false 
and timestamp >= entity.last_preview


# Resources

## /users

resource | method | description 
---------|--------|------------
/users | GET | get all users for current account - all users that has gtd tags(not followed of listed)
/users/${username}/tags|GET|list all tag objects assigned to user
/users/${username}/tags|PUT|revieve list of new tag names and merge them with already assigned to the username


## /tags
resource | method | description 
---------|--------|------------
/tags|GET|all user tags
/tags/${tagname}/users|GET|provide full list of users by tag
/tags/${tagname}/updates|GET|get all tag updates with viewed == false and timestamp >= last_view
/tags/${tagname}/updates/count|GET|get count of tag updates with viewed == false and timestamp >= last_view

System will create special lists "twitter" and "facebook" to place into friends
from facebook and twitter followed users.  

Twitter adapter will recognize all lists as tags e.g. if twitter user has list
"smart developers" system will recognise it as a tag "smart developers" and
will be accessible by /tags/smart%20developers/users

## /lists

resource | method | description 
---------|--------|------------
/lists|GET|provide a list of all smart lists for current account
/lists|PUT|recieve list of "list" objects, extract tags and users. Tag all extracted users by extracted tags per list.
/lists/${listname}/tags|GET| list of tags for listname
/lists/${listname}/tags|PUT| revieve list of tags. Tag all listed users by these tags unless not tagged yet.
/lists/${listname}/users|GET|list of users for listname
/lists/${listname}/users|PUT| recieve list of users and tag them by all listname tags unless them untagged yet.

# Sync
Will sync each time when user requests updates but not ofthen that 5(?of 15 or can be configurable) mins.


# Export
System can export all organized data back to twitter. Smart list would be
broken down by tags and exported as a twitter lists and then added new list
which will contains all users of smart list.

For example: smartlist#1 contains users with tag#1 and tag#2. Export tool will
create tag#1 and tag#2 lists and put all tagged by each tag users into related
list. Then system creates one more list smartlist#1 and put into all usesrs
tagged by both these tags.
