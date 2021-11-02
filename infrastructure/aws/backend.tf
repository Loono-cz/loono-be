# ----------
# ECR (Docker repositories)
# ----------

resource "aws_ecr_repository" "backend" {
  name                 = "${var.codename}-backend"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

# -------------
# Security Groups
# -------------

resource "aws_security_group" "backend-sg" {
  name        = "${var.codename}-backend-sg"
  description = "Backend security group."
  vpc_id      = aws_vpc.vpc.id

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [
      "10.0.0.0/16"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
}

# -------------
# IAM roles
# -------------

resource "aws_iam_role" "ecs-task-execution-role" {
  name = "${var.codename}-ecs-task-execution-role"

  assume_role_policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [
      {
        Action    = "sts:AssumeRole",
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
        Effect    = "Allow",
        Sid       = ""
      },
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs-task-execution-policy-attachment" {
  role       = aws_iam_role.ecs-task-execution-role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# --------
# service-transformation
# --------

resource "aws_ecs_cluster" "backend" {
  name = "${var.codename}-backend"
}

resource "aws_ecs_task_definition" "backend" {
  family                = "${var.codename}-backend"
  container_definitions = templatefile("ecs/backend.tmpl", {
    aws-region             = var.aws-region,
    aws-repository         = aws_ecr_repository.backend.repository_url,
    postgre-url            = "${aws_route53_record.database.fqdn}:5432/${aws_db_instance.database.name}",
    postgre-user           = var.database-username,
    postgre-pwd            = var.database-password,
    google-app-credentials = var.google-app-credentials,
    container-name         = "${var.codename}-backend",
  })
  network_mode          = "awsvpc"
  execution_role_arn    = aws_iam_role.ecs-task-execution-role.arn
  task_role_arn         = aws_iam_role.ecs-task-execution-role.arn
  memory                = "1024"
  cpu                   = "256"
}

resource "aws_ecs_service" "backend" {
  name                               = "${var.codename}-backend"
  cluster                            = aws_ecs_cluster.backend.id
  task_definition                    = aws_ecs_task_definition.backend.arn
  launch_type                        = "FARGATE"
  desired_count                      = 1
  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200
  health_check_grace_period_seconds  = 20

  network_configuration {
    security_groups = [
      aws_security_group.private-default-sg.id]
    subnets         = [
      aws_subnet.private.id
    ]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.backend-tg.arn
    container_name   = "${var.codename}-backend"
    container_port   = 8080
  }
}

resource "aws_cloudwatch_log_group" "backend-lg" {
  name = "/ecs/backend"
}

# --------------
# Load Balancers
# --------------

resource "aws_lb" "backend-lb" {
  name               = "${var.codename}-backend-lb"
  internal           = false
  load_balancer_type = "application"

  enable_deletion_protection = true

  subnets         = [
    aws_subnet.public.id,
    aws_subnet.db-private.id]
  security_groups = [
    aws_security_group.backend-sg.id]
}

resource "aws_lb_listener" "backend-elb-listener" {
  load_balancer_arn = aws_lb.backend-lb.arn
  port              = "443"
  protocol          = "HTTPS"
  certificate_arn   = var.certificate-arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.backend-tg.arn
  }
}

resource "aws_lb_listener" "backend-http-redirect" {
  load_balancer_arn = aws_lb.backend-lb.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

resource "aws_lb_target_group" "backend-tg" {
  name        = "${var.codename}-backend-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.vpc.id
  target_type = "ip"
  health_check {
    path                = "/actuator/health"
    unhealthy_threshold = 10
    enabled             = true
    port                = 8080
    matcher             = "200"
  }
}
