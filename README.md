# Service

## Domain model objects

### User

```json
{
    name: @username,
    first_tags: [<tag_obj>,...], //first 5 tags
    tags: "/users/@username/tags", //hyperlink to all user tags
    last_preview: <timestamp>,
    //hyperlink to user updates since last_preview except updates>previewed=true
    updates: "/users/@username/updates"
    updates_count: "/users/@username/updates/count"
```
### Tag
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


### Update

```json
{
    user: @username,
    timestamp: <ts>,
    previewed: false|true,
    text: <string>
}
```


## Operations

