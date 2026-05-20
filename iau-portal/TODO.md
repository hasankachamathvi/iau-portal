# TODO - MySQL + Admin sidebar/pages

## Step 1: Implement missing admin sidebar pages
- [ ] Add `GET /admin/users` endpoint in `AdminController`
- [ ] Add `GET /admin/settings` endpoint in `AdminController`
- [ ] Create Thymeleaf templates:
  - [ ] `src/main/resources/templates/admin/users.html`
  - [ ] `src/main/resources/templates/admin/settings.html`

## Step 2: Validate MySQL connection
- [ ] Ensure `spring.datasource.password` in `application.properties` matches MySQL root password (currently empty)

## Step 3: Verify functionality
- [ ] Run app and check `/admin/dashboard` loads data (counts + complaint list)
- [ ] Check complaint detail page `/admin/complaint/{crn}`
- [ ] Check `/admin/audit-logs` works

